package lbd.Utils;

import java.io.*;
import javax.swing.*;
import javax.sound.sampled.*;

/**
 * <p>Special thanks to Matthias Pfisterer, without whom I never would have
 * figured out the ridiculously complicated Java Sound API.</p>
 * 
 * <p>This is what helped me the most, and it will probably be useful to
 * future developers, so I am linking it here:
 * <a href="http://www.jsresources.org/examples/AudioPlayer.html">
 * Matthias Pfisterer's AudioPlayer.java</a>.</p>
 * 
 * <p>Also, this seems to be a decently powerful and free program for
 * converting sound files from one format to another:
 * <a href="http://www.nch.com.au/switch/">Switch Audio Converter</a>.
 * 
 * @author Stephen G. Ware
 */
public class SoundToClass {
	
	// Supported file types
	public final static String[] SUPPORTED = getSupportedFileTypes();
	// Graphical user interface

	// Main method
	public static void play(String args[]){
		// Assume command line version
		SoundToClass stc = new SoundToClass(false);
		try{
			// No arguments means GUI version
			if(args.length == 0){
				stc = new SoundToClass(true);
			}
			else{
				// Help
				if(args[0].equals("help") || args[0].equals("-help") || args[0].equals("--help") || args[0].equals("/help")) usage();
				// Too many arguments
				if(args.length > 2) usage();
				// Process arguments
				String src = null;
				String dst = null;
				if(supported(args[0]) && !removeExtension(args[0]).equals("")) src = args[0];
				else usage();
				if(args.length == 2) dst = className(removeExtension(args[1])) + ".java";
				else dst = getDefaultDst(src);
				// Convert image
				stc.soundToClass(src, dst);
				Runtime.getRuntime().exit(0);
			}
		}
		// Error if any exceptions arose
		catch(Exception ex){
			stc.error(ex);
		}
	}
	
	// Display usage information
	public static void usage(){
		String message = "\nSoundToClass by Stephen Ware\nUsage: java -jar SoundToClass.jar sound/path/and/name.ext [destination/path/and/name.java]\nDestination defaults to same directory, same file name.\nSupported formats: ";
		for(int i=0; i<SUPPORTED.length; i++){
			message += SUPPORTED[i];
			if(i+1 < SUPPORTED.length) message += ", ";
			else message += "\n";
		}
		System.out.println(message);
		Runtime.getRuntime().exit(1);
	}
	
	// Simple runnable to execute GUI creation on event dispatching thread
	private class GUImaker implements Runnable {
		private final SoundToClass stc;
		private boolean done = false;
		public GUImaker(SoundToClass itc){ this.stc = itc; }
		public void run(){ done = true; }
	}
	
	// Constructor
	public SoundToClass(boolean gui){
		// Setup GUI, if any
		if(gui){
			GUImaker guimaker = new GUImaker(this);
			SwingUtilities.invokeLater(guimaker);
		}
	}
	
	// Alert the user
	public void alert(String message, int icon){
			System.out.println(message);
	}
	
	// Convenience alert
	public void alert(String message){
		alert(message, JOptionPane.INFORMATION_MESSAGE);
	}
	
	// Handle errors
	public void error(String message, Exception ex){
		if(message == null && ex != null){
			// Unwind stack trace
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			message = sw.toString();
		}
		// Alert
		alert(message, JOptionPane.ERROR_MESSAGE);
	}
	
	// Convenience error
	public void error(Exception ex){
		error(null, ex);
	}
	
	// Convenience error
	public void error(String message){
		error(message, null);
	}
	
	// Convert sound to java code
	public void soundToClass(String srcPath, String dstPath){
		// Convert source sound into byte array
		File srcFile = new File(srcPath);
		if(srcFile.length() > Integer.MAX_VALUE){ error("The source file is too large."); return; }
		AudioInputStream audioIn = null;
		AudioFormat format = null;
		DataLine.Info lineInfo = null;
		byte[] bytes = new byte[0];
		try{
			// Attempt to open an audio input stream to this file
			audioIn = AudioSystem.getAudioInputStream(srcFile);
			format = audioIn.getFormat();
			lineInfo = new DataLine.Info(SourceDataLine.class, format, AudioSystem.NOT_SPECIFIED);
			// Check if file can be played
			try{ AudioSystem.getLine(lineInfo); }
			catch(IllegalArgumentException ex){ alert("The source file cannot be played in its current\nformat; attempting to convert to PCM."); }
			catch(LineUnavailableException ex){}
			// Convert everything to PCM_UNSIGNED encoding, if possible
			AudioFormat.Encoding targetEncoding = AudioFormat.Encoding.PCM_UNSIGNED;
			try{
				audioIn = AudioSystem.getAudioInputStream(targetEncoding, audioIn);
				format = audioIn.getFormat();
			}
			catch(IllegalArgumentException ex){
				// Otherwise, convert everything to PCM_SIGNED encoding, if possible
				targetEncoding = AudioFormat.Encoding.PCM_SIGNED;
				try{
					audioIn = AudioSystem.getAudioInputStream(targetEncoding, audioIn);
					format = audioIn.getFormat();
				}
				catch(IllegalArgumentException ex2){ alert("This file cannot be converted to PCM encoding\nand so may not play on other computers."); }
			}
			// Convert the stream data to bytes
			if(audioIn.getFrameLength() * (long)(format.getFrameSize()) > Integer.MAX_VALUE){ error("The converted source file is too large."); return; }
			bytes = getStreamAsBytes(audioIn);
			if(bytes == null) return;
		}
		catch(FileNotFoundException ex){ error("The source file does not exist."); return; }
		catch(UnsupportedAudioFileException ex){ error("The source file is not of a recognized format."); return; }
		catch(IOException ex){ error("A problem occured while reading the input file."); return; }
		// Write to destination file
		File dstFile = new File(dstPath);
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(dstFile));
			// Header
			out.write(getHeader(srcFile.getName(), removeExtension(dstFile.getName())));
			out.write(getSerialization(bytes.length));
			// Byte array(s)
			int chunk = 1024;
			int length = 0;
			int numArrays = 0;
			double progress;
			while(length < bytes.length){
				out.write("\tprivate static byte[] data" + numArrays + "(){ return new byte[] {");
				for(int i=0; ((i<chunk)&&(length<bytes.length)); i++){
					out.write(Integer.toString((int)(bytes[length])));
					if(i+1<chunk && length+1<bytes.length) out.write(",");
					length++;
					// Progress (if GUI)
					progress = ((double)(length)) / ((double)(bytes.length)) * 0.5;
					if(progress > 0.5) progress = 0.5;
				}
				out.write("}; }\n");
				numArrays++;
			}
			// Glue method
			out.write("\tprivate static byte[] getData(){\n");
			out.write("\t\tbyte[] data = new byte[" + bytes.length + "];\n");
			for(int i=0; i<numArrays; i++){
				out.write("\t\tSystem.arraycopy(data" + i + "(), 0, data, " + (i * chunk) + ", " + Math.min(chunk, (bytes.length - (i * chunk))) + ");\n");
			}
			out.write("\t\treturn data;\n");
			out.write("\t}\n");
			// Constructor
			out.write("\t/** Constructs a new AudioClip with the data from " + srcFile.getName() + ". */\n");
			out.write("\tpublic " + removeExtension(dstFile.getName()) + "(){\n");
			out.write("\t\tdata = getData();\n");
			out.write(audioFormatToCodeString(format, 2));
			out.write("\t\tlineInfo = new DataLine.Info(SourceDataLine.class, format, AudioSystem.NOT_SPECIFIED);\n");
			out.write("\t\ttry{ AudioSystem.getLine(lineInfo); }\n");
			out.write("\t\tcatch(IllegalArgumentException ex){ lineInfo = null; }\n");
			out.write("\t\tcatch(LineUnavailableException e){}\n");
			out.write("\t}\n");
			// Java footer
			out.write(getPlayThread(srcFile.getName()));
			out.write(getLoopThread(srcFile.getName(), removeExtension(dstFile.getName())));
			out.write(getPlay(srcFile.getName()));
			out.write(getLoop(srcFile.getName()));
			out.write(getStop(srcFile.getName()));
			out.write(getIsPlaying(srcFile.getName()));
			out.write(getIsLooping(srcFile.getName()));
			out.write("}");
			out.close();
		}
		catch(IOException ex){ error("An error occured while writing to the output file."); return; }
		// Alert user to success
		alert(srcPath + "\nhas been converted to\n" + dstPath);
	}
	
	// Read an input stream into a byte array
	private byte[] getStreamAsBytes(AudioInputStream stream) throws IOException {
		int frameSize = stream.getFormat().getFrameSize();
		byte[] frame = new byte[frameSize];
		byte[] data = new byte[0];
		int size = 0;
		int read = 0;
		double progress = 0.0;
		double streamSize = (double)(stream.getFrameLength());
		while(read != -1){
			read = stream.read(frame, 0, frameSize);
			if(read != -1){
				byte[] biggerData = new byte[data.length + frameSize];
				if(data.length != 0) System.arraycopy(data, 0, biggerData, 0, data.length);
				data = biggerData;
				System.arraycopy(frame, 0, data, size, frameSize);
				size += frameSize;
				// Progress for GUI
				progress = ((double)(size)/((double)(streamSize) * (double)(frameSize)));
				progress = Math.min(progress, 1.0) * 0.5;
			}
		}
		stream.close();
		return data;
	}
	
	// Get the code needed to reproduce an AudioFileFormat
	private static String audioFormatToCodeString(AudioFormat format, int indent){
		String ind = "";
		for(int i=0; i<indent; i++) ind += "\t";
		String code = "";
		// Convert format to code
		code += ind + "format = new AudioFormat(AudioFormat.Encoding." + format.getEncoding().toString().toUpperCase() + ", (float)(" + format.getSampleRate() + "), " + format.getSampleSizeInBits() + ", " + format.getChannels() + ", " + format.getFrameSize() + ", (float)(" + format.getFrameRate() + "), " + format.isBigEndian() + ");\n";
		return code;
	}
	
	// Get a list of supported formats
	private static String[] getSupportedFileTypes(){
		AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
		String[] supported = new String[types.length];
		for(int i=0; i<types.length; i++){
			supported[i] = types[i].getExtension().toUpperCase();
		}
		return supported;
	}
	
	// Get the file extension or null
	public static String getFileExtension(String file){
		if(file == null) return null;
		if(file.length() == 0) return null;
		String c;
		for(int i=file.length()-1; i>=0; i--){
			c = file.substring(i, i+1);
			if(c.equals(".")) return file.substring(i+1);
			else if(c.equals("/") || c.equals("\\")) return null;
		}
		return null;
	}
	
	// Is the file extension a supported format?
	public static boolean supported(String file){
		String extension = getFileExtension(file);
		if(extension == null) return false;
		extension = extension.toUpperCase();
		for(int i=0; i<SUPPORTED.length; i++){
			if(SUPPORTED[i].equals(extension)) return true;
		}
		return false;
	}
	
	// Get the file path and name without its extension
	public static String removeExtension(String file){
		String extension = getFileExtension(file);
		if(extension == null) return file;
		return file.substring(0, file.length() - extension.length() - 1);
	}
	
	// Convert file name to legal class name, leaving path and extension in tact
	private static String className(String file){
		// Extension
		String extension = getFileExtension(file);
		if(extension == null) extension = "";
		// Path and name
		String path = removeExtension(file);
		if(path.length() == 0) return extension;
		int i = path.length();
		String c = "";
		while(i != 0){
			i--;
			if(i != 0) c = path.substring(i, i+1);
			if(c.equals("\\")) break;
			if(c.equals("/")) break;
		}
		String name = path.substring(i+1);
		if(i != -1) path = path.substring(0, i+1);
		else path = "";		
		// Remove illegal characters from name
		String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
		String newName = "";
		for(i=0; i<name.length(); i++){
			if(allowed.indexOf(name.substring(i, i+1)) != -1) newName += name.substring(i, i+1);
		}
		if(extension != "") extension = "." + extension;
		return path + newName + extension;
	}
	
	// Get the default destination file based on source file
	public static String getDefaultDst(String src){
		return className(removeExtension(src)) + ".java";
	}
	
	// Get parts of the java file header
	private static String getHeader(String soundName, String className){
		String header = "";
		// Imports
		header += "import java.applet.AudioClip;\nimport javax.sound.sampled.AudioFormat;\nimport javax.sound.sampled.AudioSystem;\nimport javax.sound.sampled.DataLine;\nimport javax.sound.sampled.LineUnavailableException;\nimport javax.sound.sampled.SourceDataLine;\n";
		// Top JavaDoc
		header += "/** <p>An object implementing {@link java.applet.AudioClip java.applet.AudioClip} with the data from " + soundName + " hard-coded into it.</p>\n * <p>Created with the <a href=\"http://stephengware.com/projects/soundtoclass\">SoundToClass tool</a>, by Stephen G. Ware.</p>\n * @author Stephen G. Ware */\n";
		// Class definition
		header += "public class " + className + " implements AudioClip {\n";
		// Instance variables
		header += "\tprivate byte[] data;\n\tprivate AudioFormat format;\n\tprivate DataLine.Info lineInfo = null;\n\tprivate PlayThread playThread = null;\n\tprivate LoopThread loopThread = null;\n";
		return header;
	}
	private static String getSerialization(long version){
		return "\tprivate static final long serialVersionUID = " + version + ";\n";
	}
	// Get parts of the java footer
	private static String getPlayThread(String soundName){
		String playThread = "";
		playThread += "\t/** A separate thread for playing " + soundName + ". */\n";
		playThread += "\tprivate class PlayThread extends Thread {\n";
		playThread += "\t\tprivate byte[] data;\n\t\tprivate AudioFormat format;\n\t\tprivate DataLine.Info lineInfo;\n\t\tprivate SourceDataLine line = null;\n\t\tprivate boolean playing = true;\n";
		playThread += "\t\tpublic PlayThread(byte[] d, AudioFormat f, DataLine.Info i){ data = d; format = f; lineInfo = i; }\n";
		playThread += "\t\tpublic void run(){\n\t\t\ttry{\n\t\t\t\tline = (SourceDataLine) AudioSystem.getLine(lineInfo);\n\t\t\t\tline.open(format, AudioSystem.NOT_SPECIFIED);\n\t\t\t\tline.start();\n\t\t\t\tint written = 0;\n\t\t\t\tint available;\n\t\t\t\twhile(written < data.length && playing){\n\t\t\t\t\tavailable = Math.min(line.available(), data.length - written);\n\t\t\t\t\tline.write(data, written, available);\n\t\t\t\t\twritten += available;\n\t\t\t\t}\n\t\t\t\tint frames = data.length / format.getFrameSize();\n\t\t\t\twhile(line.getFramePosition() < frames && playing) Thread.sleep(0);\n\t\t\t}\n\t\t\tcatch(InterruptedException ex){ playing = false; }\n\t\t\tcatch(LineUnavailableException ex){}\n\t\t\tif(line != null){ line.stop(); line.flush(); line.close(); }\n\t\t\tplaying = false;\n\t\t}\n";
		playThread += "\t\tpublic void interrupt(){ playing = false; }\n";
		playThread += "\t\tpublic boolean isPlaying(){ return playing; }\n\t}\n";
		return playThread;
	}
	private static String getLoopThread(String soundName, String className){
		String loopThread = "";
		loopThread += "\t/** A separate thread for looping play of " + soundName + ". */\n";
		loopThread += "\tprivate class LoopThread extends Thread {\n";
		loopThread += "\t\tprivate " + className + " clip;\n\t\tprivate boolean looping = true;\n";
		loopThread += "\t\tpublic LoopThread(" + className + " c){ clip = c; }\n";
		loopThread += "\t\tpublic void run(){\n\t\t\twhile(looping){\n\t\t\t\tclip.play();\n\t\t\t\twhile(clip.isPlaying() && looping){\n\t\t\t\t\ttry{ Thread.sleep(0); }\n\t\t\t\t\tcatch(InterruptedException ex){ looping = false; break; }\n\t\t\t\t}\n\t\t\t}\n\t\t\tif(!clip.isLooping()) clip.stop();\n\t\t}\n";
		loopThread += "\t\tpublic void interrupt(){ looping = false; }\n";
		loopThread += "\t\tpublic boolean isLooping(){ return looping; }\n\t}\n";
		return loopThread;
	}
	private static String getPlay(String soundName){
		String play = "";
		play += "\t/** Plays " + soundName + " from the beginning, even if it is already playing or looping. */\n";
		play += "\tpublic void play(){ if(lineInfo == null) return; doPlay(); }\n";
		play += "\tprivate synchronized void doPlay(){\n\t\tdoStopPlay();\n\t\tplayThread = new PlayThread(data, format, lineInfo);\n\t\tplayThread.start();\n\t}\n";
		return play;
	}
	private static String getLoop(String soundName){
		String loop = "";
		loop += "\t/** Plays " + soundName + " continuously until stopped. */\n";
		loop += "\tpublic void loop(){ if(lineInfo == null) return; doLoop(); }\n";
		loop += "\tprivate synchronized void doLoop(){\n\t\tdoStopLoop();\n\t\tloopThread = new LoopThread(this);\n\t\tloopThread.start();\n\t}\n";
		return loop;
	}
	private static String getStop(String soundName){
		String stop = "";
		stop += "\t/** Stops play and looping of " + soundName + ". */\n";
		stop += "\tpublic void stop(){ if(lineInfo == null) return; doStop(); }\n";
		stop += "\tprivate synchronized void doStop(){\n\t\tdoStopPlay();\n\t\tdoStopLoop();\n\t}\n";
		stop += "\tprivate void doStopPlay(){\n\t\tif(playThread == null) return;\n\t\tif(playThread.isPlaying()) playThread.interrupt();\n\t\tplayThread = null;\n\t}\n";
		stop += "\tprivate void doStopLoop(){\n\t\tif(loopThread == null) return;\n\t\tif(loopThread.isLooping()) loopThread.interrupt();\n\t\tloopThread = null;\n\t}\n";
		return stop;
	}
	private static String getIsPlaying(String soundName){
		String isPlaying = "";
		isPlaying += "\t/** Tests if " + soundName + " is currently playing or looping.\n\t * @return <tt>true</tt> if playing or looping, <tt>false</tt> otherwise */\n";
		isPlaying += "\tpublic boolean isPlaying(){ if(lineInfo == null) return false; return doIsPlaying(); }\n";
		isPlaying += "\tprivate synchronized boolean doIsPlaying(){\n\t\tif(loopThread == null && playThread == null) return false;\n\t\telse if(loopThread == null) return playThread.isPlaying();\n\t\telse if(playThread == null) return loopThread.isLooping();\n\t\telse return loopThread.isLooping() && playThread.isPlaying();\n\t}\n";
		return isPlaying;
	}
	private static String getIsLooping(String soundName){
		String isLooping = "";
		isLooping += "\t/** Tests if " + soundName + " is currently looping.\n\t * @return <tt>true</tt> if looping, <tt>false</tt> otherwise */\n";
		isLooping += "\tpublic boolean isLooping(){ if(lineInfo == null) return false; return doIsLooping(); }\n";
		isLooping += "\tprivate synchronized boolean doIsLooping(){\n\t\tif(loopThread == null) return false;\n\t\telse return loopThread.isLooping();\n\t}\n";
		return isLooping;
	}

}
