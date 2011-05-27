package nl.captcha.audio;

// Wav file IO class
// A.Greensted
// http://www.labbookpages.co.uk

// File format is based on the information from
// http://www.sonicspot.com/guide/wavefiles.html
// http://www.blitter.com/~russtopia/MIDI/~jglatt/tech/wave.htm

// Version 1.0

import java.io.*;

public class WavFile {
    private enum IOState {
        READING, WRITING, CLOSED
    };

    private final static int BUFFER_SIZE = 4096;

    private final static int FMT_CHUNK_ID = 0x20746D66;
    private final static int DATA_CHUNK_ID = 0x61746164;
    private final static int RIFF_CHUNK_ID = 0x46464952;
    private final static int RIFF_TYPE_ID = 0x45564157;

    private IOState ioState; // Specifies the IO State of the Wav File (used for
                             // sanity checking)
    private int bytesPerSample; // Number of bytes required to store a single
                                // sample
    private long numFrames; // Number of frames within the data section
    private FileOutputStream oStream; // Output stream used for writing data
    private InputStream iStream; // Input stream used for reading data
    private double floatScale; // Scaling factor used for int <-> float
                               // conversion
    private double floatOffset; // Offset factor used for int <-> float
                                // conversion
    private boolean wordAlignAdjust; // Specify if an extra byte at the end of
                                     // the data chunk is required for word
                                     // alignment

    // Wav Header
    private int numChannels; // 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
    private long sampleRate; // 4 bytes unsigned, 0x00000001 (1) to 0xFFFFFFFF
                             // (4,294,967,295)
                             // Although a java int is 4 bytes, it is signed, so
                             // need to use a long
    private int blockAlign;     // 2 bytes unsigned, 0x0001 (1) to 0xFFFF (65,535)
    private int validBits;      // 2 bytes unsigned, 0x0002 (2) to 0xFFFF (65,535)

    // Buffering
    private byte[] buffer; // Local buffer used for IO
    private int bufferPointer; // Points to the current position in local buffer
    private int bytesRead; // Bytes read after last read into local buffer
    private long frameCounter; // Current number of frames read or written

    // Cannot instantiate WavFile directly, must either use newWavFile() or
    // openWavFile()
    private WavFile() {
        buffer = new byte[BUFFER_SIZE];
    }

    public int getNumChannels() {
        return numChannels;
    }

    public long getNumFrames() {
        return numFrames;
    }

    public long getFramesRemaining() {
        return numFrames - frameCounter;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public int getValidBits() {
        return validBits;
    }

    public static WavFile newWavFile(int numChannels,
            long numFrames, int validBits, long sampleRate) throws IOException,
            IOException {
        // Instantiate new Wavfile and initialise
        WavFile wavFile = new WavFile();
        wavFile.numChannels = numChannels;
        wavFile.numFrames = numFrames;
        wavFile.sampleRate = sampleRate;
        wavFile.bytesPerSample = (validBits + 7) / 8;
        wavFile.blockAlign = wavFile.bytesPerSample * numChannels;
        wavFile.validBits = validBits;

        // Sanity check arguments
        if (numChannels < 1 || numChannels > 65535)
            throw new IOException(
                    "Illegal number of channels, valid range 1 to 65536");
        if (numFrames < 0)
            throw new IOException("Number of frames must be positive");
        if (validBits < 2 || validBits > 65535)
            throw new IOException(
                    "Illegal number of valid bits. Valid range is 2 to 65536, got " + validBits);
        if (sampleRate < 0)
            throw new IOException("Sample rate must be positive.");

        // Calculate the chunk sizes
        long dataChunkSize = wavFile.blockAlign * numFrames;
        long mainChunkSize = 4 + // Riff Type
                8 + // Format ID and size
                16 + // Format data
                8 + // Data ID and size
                dataChunkSize;

        // Chunks must be word aligned, so if odd number of audio data bytes
        // adjust the main chunk size
        if (dataChunkSize % 2 == 1) {
            mainChunkSize += 1;
            wavFile.wordAlignAdjust = true;
        } else {
            wavFile.wordAlignAdjust = false;
        }

        // Set the main chunk size
        putLE(RIFF_CHUNK_ID, wavFile.buffer, 0, 4);
        putLE(mainChunkSize, wavFile.buffer, 4, 4);
        putLE(RIFF_TYPE_ID, wavFile.buffer, 8, 4);

        // Write out the header
        wavFile.oStream.write(wavFile.buffer, 0, 12);

        // Put format data in buffer
        long averageBytesPerSecond = sampleRate * wavFile.blockAlign;

        putLE(FMT_CHUNK_ID, wavFile.buffer, 0, 4); // Chunk ID
        putLE(16, wavFile.buffer, 4, 4); // Chunk Data Size
        putLE(1, wavFile.buffer, 8, 2); // Compression Code (Uncompressed)
        putLE(numChannels, wavFile.buffer, 10, 2); // Number of channels
        putLE(sampleRate, wavFile.buffer, 12, 4); // Sample Rate
        putLE(averageBytesPerSecond, wavFile.buffer, 16, 4); // Average Bytes
                                                             // Per Second
        putLE(wavFile.blockAlign, wavFile.buffer, 20, 2); // Block Align
        putLE(validBits, wavFile.buffer, 22, 2); // Valid Bits

        // Write Format Chunk
        wavFile.oStream.write(wavFile.buffer, 0, 24);

        // Start Data Chunk
        putLE(DATA_CHUNK_ID, wavFile.buffer, 0, 4); // Chunk ID
        putLE(dataChunkSize, wavFile.buffer, 4, 4); // Chunk Data Size

        // Write Format Chunk
        wavFile.oStream.write(wavFile.buffer, 0, 8);

        // Calculate the scaling factor for converting to a normalised double
        if (wavFile.validBits > 8) {
            // If more than 8 validBits, data is signed
            // Conversion required multiplying by magnitude of max positive
            // value
            wavFile.floatOffset = 0;
            wavFile.floatScale = Long.MAX_VALUE >> (64 - wavFile.validBits);
        } else {
            // Else if 8 or less validBits, data is unsigned
            // Conversion required dividing by max positive value
            wavFile.floatOffset = 1;
            wavFile.floatScale = 0.5 * ((1 << wavFile.validBits) - 1);
        }

        // Finally, set the IO State
        wavFile.bufferPointer = 0;
        wavFile.bytesRead = 0;
        wavFile.frameCounter = 0;
        wavFile.ioState = IOState.WRITING;

        return wavFile;
    }

    public static WavFile openWavFile(InputStream is) throws IOException,
            IOException {
        WavFile wav = new WavFile();
        
        wav.iStream = is;

        // Read the first 12 bytes of the file
        int bytesRead = is.read(wav.buffer, 0, 12);
        if (bytesRead != 12) {
            throw new IOException("Not enough bytes in InputStream for header.");
        }

        // Extract parts from the header
        long riffChunkID = getLE(wav.buffer, 0, 4);
        long chunkSize = getLE(wav.buffer, 4, 4);
        long riffTypeID = getLE(wav.buffer, 8, 4);

        // Check the header bytes contains the correct signature
        if (riffChunkID != RIFF_CHUNK_ID) {
            throw new IOException(
                    "Invalid WAV Header data: incorrect riff chunk ID.");
        }
        if (riffTypeID != RIFF_TYPE_ID) {
            throw new IOException(
                    "Invalid WAV Header data: incorrect riff type ID.");
        }

        // Check that the file size matches the number of bytes listed in header
        // if (file.length() != chunkSize+8) {
        // throw new IOException("Header chunk size (" + chunkSize +
        // ") does not match file size (" + file.length() + ")");
        // }

        boolean foundFormat = false;
        boolean foundData = false;

        // Search for the Format and Data Chunks
        while (true) {
            // Read the first 8 bytes of the chunk (ID and chunk size)
            bytesRead = is.read(wav.buffer, 0, 8);
            if (bytesRead == -1) {
                throw new IOException(
                        "Reached end of file without finding format chunk");
            }
            if (bytesRead != 8) {
                throw new IOException("Could not read chunk header");
            }

            // Extract the chunk ID and Size
            long chunkId = getLE(wav.buffer, 0, 4);
            chunkSize = getLE(wav.buffer, 4, 4);

            // Word align the chunk size
            // chunkSize specifies the number of bytes holding data. However,
            // the data should be word aligned (2 bytes) so we need to calculate
            // the actual number of bytes in the chunk
            long numChunkBytes = (chunkSize % 2 == 1) ? chunkSize + 1
                    : chunkSize;

            if (chunkId == FMT_CHUNK_ID) {
                // Flag that the format chunk has been found
                foundFormat = true;

                // Read in the header info
                bytesRead = is.read(wav.buffer, 0, 16);

                // Check this is uncompressed data
                int compressionCode = (int) getLE(wav.buffer, 0, 2);
                if (compressionCode != 1) {
                    throw new IOException("Compression Code "
                            + compressionCode + " not supported.");
                }

                // Extract the format information
                wav.numChannels = (int) getLE(wav.buffer, 2, 2);
                wav.sampleRate = getLE(wav.buffer, 4, 4);
                wav.blockAlign = (int) getLE(wav.buffer, 12, 2);
                wav.validBits = (int) getLE(wav.buffer, 14, 2);

                if (wav.numChannels == 0) {
                    throw new IOException(
                            "Number of channels must not be zero.");
                }
                if (wav.blockAlign == 0) {
                    throw new IOException(
                            "Block Align must not be zero.");
                }
                if (wav.validBits < 2) {
                    throw new IOException(
                            "Valid Bits must be greater than two.");
                }
                if (wav.validBits > 64) {
                    throw new IOException(
                            "Valid Bits specified in header is greater than 64, this is greater than a long can hold.");
                }

                // Calculate the number of bytes required to hold 1 sample
                wav.bytesPerSample = (wav.validBits + 7) / 8;
                if (wav.bytesPerSample * wav.numChannels != wav.blockAlign)
                    throw new IOException(
                            "Block Align does not agree with bytes required for validBits and number of channels");

                // Account for number of format bytes and then skip over
                // any extra format bytes
                numChunkBytes -= 16;
                if (numChunkBytes > 0)
                    wav.iStream.skip(numChunkBytes);
            } else if (chunkId == DATA_CHUNK_ID) {
                // Check if we've found the format chunk,
                // If not, throw an exception as we need the format information
                // before we can read the data chunk
                if (foundFormat == false) {
                    throw new IOException(
                            "Data chunk found before Format chunk");
                }

                // Check that the chunkSize (wav data length) is a multiple of
                // the block align (bytes per frame)
                if (chunkSize % wav.blockAlign != 0) {
                    throw new IOException(
                            "Data Chunk size is not multiple of Block Align");
                }

                // Calculate the number of frames
                wav.numFrames = chunkSize / wav.blockAlign;

                // Flag that we've found the wave data chunk
                foundData = true;

                break;
            } else {
                // If an unknown chunk ID is found, just skip over the chunk
                // data
                is.skip(numChunkBytes);
            }
        }

        // Throw an exception if no data chunk has been found
        if (foundData == false) {
            throw new IOException("Did not find a data chunk");
        }

        // Calculate the scaling factor for converting to a normalised double
        if (wav.validBits > 8) {
            // If more than 8 validBits, data is signed
            // Conversion required dividing by magnitude of max negative value
            wav.floatOffset = 0;
            wav.floatScale = 1 << (wav.validBits - 1);
        } else {
            // Else if 8 or less validBits, data is unsigned
            // Conversion required dividing by max positive value
            wav.floatOffset = -1;
            wav.floatScale = 0.5 * ((1 << wav.validBits) - 1);
        }

        wav.bufferPointer = 0;
        wav.bytesRead = 0;
        wav.frameCounter = 0;
        wav.ioState = IOState.READING;

        return wav;
    }

    // Get and Put little endian data from local buffer
    // ------------------------------------------------
    private static long getLE(byte[] buffer, int pos, int numBytes) {
        numBytes--;
        pos += numBytes;

        long val = buffer[pos] & 0xFF;
        for (int b = 0; b < numBytes; b++)
            val = (val << 8) + (buffer[--pos] & 0xFF);

        return val;
    }

    private static void putLE(long val, byte[] buffer, int pos, int numBytes) {
        for (int b = 0; b < numBytes; b++) {
            buffer[pos] = (byte) (val & 0xFF);
            val >>= 8;
            pos++;
        }
    }

    // Sample Writing and Reading
    // --------------------------
    private void writeSample(long val) throws IOException {
        for (int b = 0; b < bytesPerSample; b++) {
            if (bufferPointer == BUFFER_SIZE) {
                oStream.write(buffer, 0, BUFFER_SIZE);
                bufferPointer = 0;
            }

            buffer[bufferPointer] = (byte) (val & 0xFF);
            val >>= 8;
            bufferPointer++;
        }
    }

    private long readSample() throws IOException {
        long val = 0;

        for (int b = 0; b < bytesPerSample; b++) {
            if (bufferPointer == bytesRead) {
                int read = iStream.read(buffer, 0, BUFFER_SIZE);
                if (read == -1)
                    throw new IOException("Not enough data available");
                bytesRead = read;
                bufferPointer = 0;
            }

            int v = buffer[bufferPointer];
            if (b < bytesPerSample - 1 || bytesPerSample == 1)
                v &= 0xFF;
            val += v << (b * 8);

            bufferPointer++;
        }

        return val;
    }

    // Integer
    // -------
    public int readFrames(int[] sampleBuffer, int numFramesToRead)
            throws IOException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(int[] sampleBuffer, int offset, int numFramesToRead)
            throws IOException {
        if (ioState != IOState.READING) {
            throw new IOException("Cannot read from WavFile instance");
        }

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[offset] = (int) readSample();
                offset++;
            }

            frameCounter++;
        }

        return numFramesToRead;
    }

    public int readFrames(int[][] sampleBuffer, int numFramesToRead)
            throws IOException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(int[][] sampleBuffer, int offset, int numFramesToRead)
            throws IOException {
        if (ioState != IOState.READING)
            throw new IOException("Cannot read from WavFile instance");

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[c][offset] = (int) readSample();
            }

            offset++;
            frameCounter++;
        }

        return numFramesToRead;
    }

    public int writeFrames(int[] sampleBuffer, int numFramesToWrite)
            throws IOException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(int[] sampleBuffer, int offset, int numFramesToWrite)
            throws IOException {
        if (ioState != IOState.WRITING) {
            throw new IOException("Cannot write to WavFile instance");
        }

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample(sampleBuffer[offset]);
                offset++;
            }

            frameCounter++;
        }

        return numFramesToWrite;
    }

    public int writeFrames(int[][] sampleBuffer, int numFramesToWrite)
            throws IOException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(int[][] sampleBuffer, int offset,
            int numFramesToWrite) throws IOException {
        if (ioState != IOState.WRITING)
            throw new IOException("Cannot write to WavFile instance");

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample(sampleBuffer[c][offset]);
            }

            offset++;
            frameCounter++;
        }

        return numFramesToWrite;
    }

    // Long
    // ----
    public int readFrames(long[] sampleBuffer, int numFramesToRead)
            throws IOException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(long[] sampleBuffer, int offset, int numFramesToRead)
            throws IOException {
        if (ioState != IOState.READING)
            throw new IOException("Cannot read from WavFile instance");

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[offset] = readSample();
                offset++;
            }

            frameCounter++;
        }

        return numFramesToRead;
    }

    public int readFrames(long[][] sampleBuffer, int numFramesToRead)
            throws IOException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(long[][] sampleBuffer, int offset, int numFramesToRead)
            throws IOException {
        if (ioState != IOState.READING)
            throw new IOException("Cannot read from WavFile instance");

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[c][offset] = readSample();
            }

            offset++;
            frameCounter++;
        }

        return numFramesToRead;
    }

    public int writeFrames(long[] sampleBuffer, int numFramesToWrite)
            throws IOException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(long[] sampleBuffer, int offset, int numFramesToWrite)
            throws IOException {
        if (ioState != IOState.WRITING)
            throw new IOException("Cannot write to WavFile instance");

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample(sampleBuffer[offset]);
                offset++;
            }

            frameCounter++;
        }

        return numFramesToWrite;
    }

    public int writeFrames(long[][] sampleBuffer, int numFramesToWrite)
            throws IOException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(long[][] sampleBuffer, int offset,
            int numFramesToWrite) throws IOException {
        if (ioState != IOState.WRITING)
            throw new IOException("Cannot write to WavFile instance");

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample(sampleBuffer[c][offset]);
            }

            offset++;
            frameCounter++;
        }

        return numFramesToWrite;
    }

    // Double
    // ------
    public int readFrames(double[] sampleBuffer, int numFramesToRead)
            throws IOException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(double[] sampleBuffer, int offset, int numFramesToRead)
            throws IOException {
        if (ioState != IOState.READING) {
            throw new IOException("Cannot read from WavFile instance");
        }

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[offset] = floatOffset + (double) readSample()
                        / floatScale;
                offset++;
            }

            frameCounter++;
        }

        return numFramesToRead;
    }

    public int readFrames(double[][] sampleBuffer, int numFramesToRead)
            throws IOException {
        return readFrames(sampleBuffer, 0, numFramesToRead);
    }

    public int readFrames(double[][] sampleBuffer, int offset,
            int numFramesToRead) throws IOException {
        if (ioState != IOState.READING)
            throw new IOException("Cannot read from WavFile instance");

        for (int f = 0; f < numFramesToRead; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                sampleBuffer[c][offset] = floatOffset + (double) readSample()
                        / floatScale;
            }

            offset++;
            frameCounter++;
        }

        return numFramesToRead;
    }

    public int writeFrames(double[] sampleBuffer, int numFramesToWrite)
            throws IOException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(double[] sampleBuffer, int offset,
            int numFramesToWrite) throws IOException {
        if (ioState != IOState.WRITING)
            throw new IOException("Cannot write to WavFile instance");

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames)
                return f;

            for (int c = 0; c < numChannels; c++) {
                writeSample((long) (floatScale * (floatOffset + sampleBuffer[offset])));
                offset++;
            }

            frameCounter++;
        }

        return numFramesToWrite;
    }

    public int writeFrames(double[][] sampleBuffer, int numFramesToWrite)
            throws IOException {
        return writeFrames(sampleBuffer, 0, numFramesToWrite);
    }

    public int writeFrames(double[][] sampleBuffer, int offset,
            int numFramesToWrite) throws IOException {
        if (ioState != IOState.WRITING) {
            throw new IOException("Cannot write to WavFile instance");
        }

        for (int f = 0; f < numFramesToWrite; f++) {
            if (frameCounter == numFrames) {
                return f;
            }

            for (int c = 0; c < numChannels; c++) {
                writeSample((long) (floatScale * (floatOffset + sampleBuffer[c][offset])));
            }

            offset++;
            frameCounter++;
        }

        return numFramesToWrite;
    }

    public void close() throws IOException {
        // Close the input stream and set to null
        if (iStream != null) {
            iStream.close();
            iStream = null;
        }

        if (oStream != null) {
            // Write out anything still in the local buffer
            if (bufferPointer > 0)
                oStream.write(buffer, 0, bufferPointer);

            // If an extra byte is required for word alignment, add it to the
            // end
            if (wordAlignAdjust)
                oStream.write(0);

            // Close the stream and set to null
            oStream.close();
            oStream = null;
        }

        // Flag that the stream is closed
        ioState = IOState.CLOSED;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Channels: %d, Frames: %d\n", numChannels,
                numFrames));
        sb.append(String.format("IO State: %s\n", ioState));
        sb.append(String.format("Sample Rate: %d, Block Align: %d\n",
                sampleRate, blockAlign));
        sb.append(String.format("Valid Bits: %d, Bytes per sample: %d\n",
                validBits, bytesPerSample));

        return sb.toString();
    }

    // public static void main(String[] args)
    // {
    // if (args.length < 1)
    // {
    // System.err.println("Must supply filename");
    // System.exit(1);
    // }
    //
    // try
    // {
    // for (String filename : args)
    // {
    // WavFile readWavFile = openWavFile(new File(filename));
    // readWavFile.toString();
    //
    // long numFrames = readWavFile.getNumFrames();
    // int numChannels = readWavFile.getNumChannels();
    // int validBits = readWavFile.getValidBits();
    // long sampleRate = readWavFile.getSampleRate();
    //
    // WavFile writeWavFile = newWavFile(new File("out.wav"), numChannels,
    // numFrames, validBits, sampleRate);
    //
    // final int BUF_SIZE = 5001;
    //
    // // int[] buffer = new int[BUF_SIZE * numChannels];
    // // long[] buffer = new long[BUF_SIZE * numChannels];
    // double[] buffer = new double[BUF_SIZE * numChannels];
    //
    // int framesRead = 0;
    // int framesWritten = 0;
    //
    // do
    // {
    // framesRead = readWavFile.readFrames(buffer, BUF_SIZE);
    // framesWritten = writeWavFile.writeFrames(buffer, BUF_SIZE);
    // System.out.printf("%d %d\n", framesRead, framesWritten);
    // }
    // while (framesRead != 0);
    //
    // readWavFile.close();
    // writeWavFile.close();
    // }
    //
    // WavFile writeWavFile = newWavFile(new File("out2.wav"), 1, 10, 23,
    // 44100);
    // double[] buffer = new double[10];
    // writeWavFile.writeFrames(buffer, 10);
    // writeWavFile.close();
    // }
    // catch (Exception e)
    // {
    // System.err.println(e);
    // e.printStackTrace();
    // }
    // }
}
