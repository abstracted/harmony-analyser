package org.harmony_analyser.plugins.vamp_plugins;

import org.harmony_analyser.plugins.*;
import org.junit.*;
import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for ChordinoPlugin class
 */

@SuppressWarnings("ConstantConditions")

public class ChordinoPluginTest {
	private ChordinoPlugin chordino;
	private File testWavFile;
	private List<String> inputFiles;

	@Before
	public void setUp() throws Exception {
		chordino = new ChordinoPlugin();
		ClassLoader classLoader = getClass().getClassLoader();
		testWavFile = new File(classLoader.getResource("test.wav").getPath());
		inputFiles = new ArrayList<>();
		inputFiles.add(testWavFile.toString());
	}

	@Test
	public void shouldExtractChords() {
		try {
			chordino.analyse(inputFiles, testWavFile.toString() + "-segmentation.txt");
			BufferedReader reader = new BufferedReader(new FileReader(testWavFile.toString() + "-segmentation.txt"));
			String line = reader.readLine();
			assertEquals(" 0.371519274: N", line);
			line = reader.readLine();
			assertEquals(" 0.464399092: B", line);
		} catch (IOException | AnalysisPlugin.IncorrectInput e) {
			e.printStackTrace();
		}
	}
}