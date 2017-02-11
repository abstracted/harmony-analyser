package org.harmony_analyser.jharmonyanalyser.services;

import org.harmony_analyser.application.visualizations.*;
import org.harmony_analyser.jharmonyanalyser.chroma_analyser.Chroma;
import org.vamp_plugins.PluginLoader;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Class to orchestrate all levels of audio analysis, using available plugins and visualizations
 */

@SuppressWarnings("SameParameterValue")
public class AudioAnalyser {
	private final AnalysisFactory analysisFactory;
	private final DataChartFactory dataChartFactory;
	public String outputWriteBuffer; // This instance variable holds the progress of the current analysis, convenient to access after the analysis is done

	public static class IncorrectInputException extends Exception {
		public IncorrectInputException(String message) {
			super(message);
		}
	}

	public static class LoadFailedException extends Exception {
		public LoadFailedException(String message) {
			super(message);
		}
	}

	public static class OutputAlreadyExists extends Exception {
		public OutputAlreadyExists(String message) {
			super(message);
		}
	}

	public static class OutputNotReady extends Exception {
		OutputNotReady(String message) {
			super(message);
		}
	}

	public static class ParseOutputError extends Exception {
		public ParseOutputError(String message) {
			super(message);
		}
	}

	// Dependency injection: AnalysisFactory, DataChartFactory
	public AudioAnalyser(AnalysisFactory analysisFactory, DataChartFactory dataChartFactory) {
		this.analysisFactory = analysisFactory;
		this.dataChartFactory = dataChartFactory;
	}

	/* Public / Package methods */

	public String[] getAllWrappedVampPlugins() { return analysisFactory.getWrappedVampPlugins(); }

	public String[] getAllChordAnalyserPlugins() { return analysisFactory.getChordAnalyserPlugins(); }

	public String[] getAllChromaAnalyserPlugins() { return analysisFactory.getChromaAnalyserPlugins(); }

	public String[] getAllPostProcessingFilters() { return analysisFactory.getPostProcessingFilters(); }

	public String[] getAllVisualizations() {
		return dataChartFactory.getAllVisualizations();
	}

	public String printPlugins() {
		String result = "";
		result += "\n> Available plugins (" + analysisFactory.getAvailablePlugins().length + "):\n";

		for (String availableAnalysisKey : analysisFactory.getAvailablePlugins()) {
			result += availableAnalysisKey + "\n";
		}

		result += "\n> Available visualizations (" + dataChartFactory.getAllVisualizations().length + "):\n";

		for (String availableAnalysisKey : dataChartFactory.getAllVisualizations()) {
			result += availableAnalysisKey + "\n";
		}

		result += printInstalledVampPlugins();

		return result;
	}

	public String printInstalledVampPlugins() {
		String result = "";
		String[] plugins = PluginLoader.getInstance().listPlugins();
		result += "\n> Locally installed VAMP plugins (" + plugins.length + "):\n";
		for (int i = 0; i < plugins.length; ++i) {
			result += i + ": " + plugins[i] + "\n";
		}

		List<String> wrappedPlugins = new ArrayList<>();
		for (int i = 0; i < plugins.length; ++i) {
			for (String wrapped_plugin : analysisFactory.getWrappedVampPlugins()) {
				if (plugins[i].equals(wrapped_plugin)) {
					wrappedPlugins.add(i + ": " + plugins[i] + "\n");
				}
			}
		}
		result += "\n> Implemented VAMP plugins (" + wrappedPlugins.size() + "):\n";
		for (String s : wrappedPlugins) {
			result += s;
		}
		return result;
	}

	public String getPluginName(String analysisKey) throws LoadFailedException {
		return analysisFactory.createPlugin(analysisKey).name;
	}

	public String getPluginDescription(String analysisKey) throws LoadFailedException {
		return analysisFactory.createPlugin(analysisKey).description;
	}

	public String printParameters(String analysisKey) throws LoadFailedException {
		return analysisFactory.createPlugin(analysisKey).printParameters();
	}

	public String runAnalysis(String inputFile, String analysisKey, boolean force, boolean verbose) throws AudioAnalyser.IncorrectInputException, OutputAlreadyExists, IOException, LoadFailedException, Chroma.WrongChromaSize {
		outputWriteBuffer = ""; // clear outputWriteBuffer
		printAndAddToBuffer(analysisFactory.createPlugin(analysisKey).analyse(inputFile, force, verbose));
		return outputWriteBuffer;
	}

	public DataChart createDataChart(String inputFile, String analysisKey) throws LoadFailedException, OutputNotReady, ParseOutputError, IOException {
		return dataChartFactory.createDataChart(analysisKey, analysisFactory.createPlugin(analysisKey).getDataFromOutput(inputFile));
	}

	// Analyses folder with given Analysis, writes to System.out as well as writes to outputWriteBuffer
	public String analyseFolder(File inputFolder, String pluginKey, String suffixAndExtension) {
		outputWriteBuffer = ""; // clear outputWriteBuffer
		String outputWrite = "";
		if (inputFolder.isFile()) {
			printAndAddToBuffer("\n> Folder needs to be selected for Audio Analysis Tool");
			return outputWriteBuffer;
		}
		try {
			printAndAddToBuffer("\n> Analyzing input folder using plugin: " + pluginKey);
			Files.walkFileTree(inputFolder.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					printAndAddToBuffer("\nDir: " + dir.toString());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (file.toString().endsWith(suffixAndExtension)) {
						printAndAddToBuffer("\nProcessing: " + file.toString() + "\n");
						try {
							printAndAddToBuffer(runAnalysis(file.toString(), pluginKey, true, false));
						} catch (AudioAnalyser.IncorrectInputException | AudioAnalyser.LoadFailedException e) {
							printAndAddToBuffer("\nERROR: " + e.getMessage());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return outputWriteBuffer;
	}

	// Prints to System.out and adds String to outputWriteBuffer at the same time
	private void printAndAddToBuffer(String outputWrite) {
		System.out.println(outputWrite);
		outputWriteBuffer += outputWrite;
	}
}
