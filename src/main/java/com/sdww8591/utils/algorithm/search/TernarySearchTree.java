package com.sdww8591.utils.algorithm.search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

/**
 * TST node
 * @author wangxuan
 *
 */
public class TernarySearchTree {

	public static class TernaryTreeNode {

		char splitchar;

		TernaryTreeNode lowerTTN;

		TernaryTreeNode currentTTN;

		TernaryTreeNode higherTTN;

		List<String> value = new LinkedList<String>();

		public TernaryTreeNode(){}

		public TernaryTreeNode(char splitchar) {
			this.splitchar = splitchar;
		}
	}

	/**
	 * build a TST
	 * @param words <key, word> index can not be empty
	 * @return
	 */
	public static TernaryTreeNode buildTernaryTree(List<Pair<char[], String>> wordList) {

		if(wordList == null || wordList.size() == 0) {

			throw new IllegalArgumentException("word array can not be empty!");
		}

		for(Pair<char[], String> word: wordList) {

			if(word.getLeft() == null || word.getLeft().length == 0) {

				throw new IllegalArgumentException("the key of word can not be empty!");
			}
		}
		
		Collections.sort(wordList, new Comparator<Pair<char[], String>>() {
			@Override
			public int compare(Pair<char[], String> o1, Pair<char[], String> o2) {

				return o1.getLeft()[0] - o2.getLeft()[0];
			}
		});

		TernaryTreeNode root = new TernaryTreeNode(wordList.get(wordList.size() / 2).getLeft()[0]);
		balancedInsert(wordList, 0 , wordList.size() - 1, root);
		return root;
	}
	
	public static void balancedInsert(List<Pair<char[], String>> wordList, int start, int end, TernaryTreeNode root) {

		if(start > end) {

			return;
		}

		int middle = (start + end) / 2;
		insert(wordList.get(middle), root);
		balancedInsert(wordList, start, middle - 1, root);
		balancedInsert(wordList, middle + 1, end, root);
	}

	public static void insert(Pair<char[], String> word, TernaryTreeNode root) {

		int index = 0;
		while(true) {

			if(word.getLeft()[index] == root.splitchar) {

				if(++index == word.getLeft().length) {

					root.value.add(word.getRight());
					return;
				}

				if(root.currentTTN == null) {

					root.currentTTN = new TernaryTreeNode(word.getLeft()[index]);
				}
				root = root.currentTTN;
			} else if(word.getLeft()[index] > root.splitchar) {

				if(root.higherTTN == null) {

					root.higherTTN = new TernaryTreeNode(word.getLeft()[index]);
				}
				root = root.higherTTN;
			} else {

				if(root.lowerTTN == null) {

					root.lowerTTN = new TernaryTreeNode(word.getLeft()[index]);
				}
				root = root.lowerTTN;
			}
		}

	}

	public static List<String> search(char[] keySeq, int depth, int max, TernaryTreeNode root) {

		if(ArrayUtils.isEmpty(keySeq)) {

			throw new IllegalArgumentException("search key sequence can not be empty!");
		}

		int index = 0;
		List<String> resultList = new LinkedList<String>();
		while (root != null && index < keySeq.length) {

			if(keySeq[index] == root.splitchar) {

				if(++index == keySeq.length) {

					resultList.addAll(root.value);
				}

				root = root.currentTTN;
			} else if(keySeq[index] < root.splitchar) {

				root = root.lowerTTN;
			} else if(keySeq[index] > root.splitchar) {

				root = root.higherTTN;
			}
		}
		
		resultList.addAll(dfsTST(root, depth));

		return resultList.subList(0, Math.min(max, resultList.size()));
	}
	
	public static List<String> dfsTST(TernaryTreeNode root, int depth) {
		
		if(depth > 0 && root != null) {
			
			List<String> resultList = new LinkedList<String>();
			resultList.addAll(root.value);
			resultList.addAll(dfsTST(root.lowerTTN, depth));
			resultList.addAll(dfsTST(root.higherTTN, depth));
			resultList.addAll(dfsTST(root.currentTTN, depth - 1));
			return resultList;
		}
		
		return Collections.emptyList();
	}
	
	public static void main(String[] args) throws Exception{
		
		String path = "";
		List<Pair<char[], String>> wordList = new LinkedList<>();
		
		try (FileReader fr = new FileReader(path);
				BufferedReader br = new BufferedReader(fr)) {
			
			String line = null;
			while( (line = br.readLine()) != null) {
				
				String word = StringUtils.trim(line);
				wordList.add(new ImmutablePair<char[], String>(word.toCharArray(), word));
				String pinyin = PinyinHelper.convertToPinyinString(word, "", PinyinFormat.WITHOUT_TONE);
				wordList.add(new ImmutablePair<char[], String>(pinyin.toCharArray(), word));
			}
		}
		
		ArrayList<Pair<char[], String>> wordArray = new ArrayList<Pair<char[], String>>(wordList);
		TernaryTreeNode root = buildTernaryTree(wordArray);
		
		List<String> resultList = search("wa".toCharArray(), Integer.MAX_VALUE, Integer.MAX_VALUE, root);
		for(String result: resultList) {
			System.out.println(result);
		}
	}
}