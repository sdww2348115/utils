package com.sdww8591.utils.algorithm.search;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

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
	public static TernaryTreeNode buildTernaryTree(Pair<char[], String>[] words) {

		if(words == null || words.length == 0) {

			throw new IllegalArgumentException("word array can not be empty!");
		}

		for(Pair<char[], String> word: words) {

			if(word.getLeft() == null || word.getLeft().length == 0) {

				throw new IllegalArgumentException("the key of word can not be empty!");
			}
		}
		
		Arrays.sort(words, new Comparator<Pair<char[], String>>() {
			@Override
			public int compare(Pair<char[], String> o1, Pair<char[], String> o2) {

				return o1.getLeft()[0] - o2.getLeft()[0];
			}
		});


		TernaryTreeNode root = new TernaryTreeNode(words[words.length / 2].getLeft()[0]);


		
		return null;
	}
	
	public static void balancedInsert(Pair<char[], String>[] words, int start, int end, TernaryTreeNode root) {

		if(start > end) {

			return;
		}

		int middle = (start + end) / 2;
		insert(words[middle], root);
		balancedInsert(words, start, middle - 1, root);
		balancedInsert(words, middle + 1, end, root);
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

		if(ArrayUtils.isNotEmpty(keySeq)) {

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

		int currentDepth = 0;
		while (root != null && currentDepth < depth) {

			resultList.addAll(root.value);
			root = root.currentTTN;
			currentDepth++;
		}

		return resultList;
	}
}
