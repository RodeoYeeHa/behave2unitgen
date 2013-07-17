package org.bom.jbehaveasmunit.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bom.jbehaveasmunit.runner.Behave2UnitGenRunner;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.RegexStoryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Class to read the stories from the filesystem
 * 
 * @author Carsten Severin
 **/
public class StoryReader {

	HashMap<String, StoryIntern> stories;

	List<String> unreadStories = new ArrayList<String>();

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public StoryReader(boolean findStories) throws IOException {
		stories = new HashMap<String, StoryIntern>();
		if (findStories) {
			int stories = findStories();
			logger.info("Found " + stories + " stories");
		}
	}

	public Story readStory(String key) throws IOException {
		StoryIntern storyIntern = stories.get(key);
		if (storyIntern == null) {
			storyIntern = new StoryIntern(key, null);
			stories.put(key, storyIntern);
			InputStream in = Behave2UnitGenRunner.class
					.getResourceAsStream(key);

			if (in == null) {
				return null;
			}
			storyIntern.story = createStory(in);
		}
		
		markStoryAsUsed(key);
		
		return storyIntern.story;
	}

	public List<String> listUnusedStories() {
		return unreadStories;
	}

	private Story createStory(InputStream in) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String next;
		StringBuffer storyText = new StringBuffer();
		while ((next = br.readLine()) != null) {
			storyText.append(next).append("\n");
		}

		return new RegexStoryParser().parseStory(storyText.toString());

	}

	private void markStoryAsUsed(String key) {
		for (Iterator<String>it = unreadStories.iterator(); it.hasNext();){
			String path = it.next();
			if (path.indexOf(key)>=0){
				it.remove();
				return;
			}
		}
	}

	public int findStories() throws IOException {
		Resource[] resources = new PathMatchingResourcePatternResolver()
				.getResources("classpath*:**/*.story");
		stories = new HashMap<String, StoryReader.StoryIntern>();

		ArrayList<String> foundPathPrefix = new ArrayList<String>();
		
		for (int i = 0; i < resources.length; i++) {
			String filenameOriginal = resources[i].getFile().getCanonicalPath();
			unreadStories.add(filenameOriginal);
		}
//		
//		for (int i = 0; i < resources.length; i++) {
//			String filenameOriginal = resources[i].getFile().getCanonicalPath();
//
//			String filename = filenameOriginal;
//			InputStream in = null;
//			boolean foundInFirstRun = false;
//			for (Iterator<String> foundPrefixesIt = foundPathPrefix.iterator(); foundPrefixesIt
//					.hasNext();) {
//				String prefix = foundPrefixesIt.next();
//				filename = filenameOriginal.replaceAll(prefix,  "");
//				in = Behave2UnitGenRunner.class.getResourceAsStream(filename);
//				if (in != null) {
//					unreadStories.add(filename);
//					foundInFirstRun = true;
//					break;
//				}
//			}
//
//			if (!foundInFirstRun) {
//				int nextIndex = -1;
//				do {
//					in = Behave2UnitGenRunner.class
//							.getResourceAsStream(filename);
//					if (in == null) {
//						// TODO: nur unter Linux/Unix???
//						nextIndex = filename.indexOf(File.separator, 1);
//						filename = filename.substring(nextIndex);
//					}
//				} while (in == null && nextIndex >= 0);
//
//				int begin = filenameOriginal.indexOf(filename);
//				foundPathPrefix.add(filenameOriginal.substring(0, begin));
//
//				unreadStories.add(filename);
//			}
//			
//		}
		return unreadStories.size();
	}

	public class StoryIntern {

		String name;
		Story story;
		boolean used;

		public StoryIntern(String name, Story story) {
			this.name = name;
			this.used = false;
			this.story = story;
		}

	}

}
