package im.service.help.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

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
	
	/**
	 * 尽量保证TernarySearchTree的平衡性
	 * @param wordList 词典
	 * @param start 待插入词序列开始下标
	 * @param end 待插入词序列结束下标
	 * @param root 词典树根节点
	 */
	public static void balancedInsert(List<Pair<char[], String>> wordList, int start, int end, TernaryTreeNode root) {

		if(start > end) {

			return;
		}

		int middle = (start + end) / 2;
		insert(wordList.get(middle), root);
		balancedInsert(wordList, start, middle - 1, root);
		balancedInsert(wordList, middle + 1, end, root);
	}

	/**
	 * 向TernaryTree中插入一个词
	 * @param word 待插入词
	 * @param root TernaryTree的根节点
	 */
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

	/**
	 * 从TernaryTree中找到某个char序列为前缀的所有词
	 * @param keySeq 待搜索的char序列
	 * @param depth 搜索深度
	 * @param max 待选结果数
	 * @param root TernaryTree的根节点
	 * @return
	 */
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
		
		resultList.addAll(bfsTST(root, depth, max));

		return resultList.subList(0, Math.min(max, resultList.size()));
	}
	
	/**
	 * 深度优先遍历一TernaryTree
	 * @param root 根节点
	 * @param depth 深度（注：TernaryTree深度的定义与普通的树不一样）
	 * @return
	 */
	public static void dfsTST(TernaryTreeNode root, int depth, int max, List<String> resultList) {
		
		if(depth > 0 && root != null && max > 0) {
			
			if(resultList == null) {
				
				resultList = new LinkedList<String>();
			}
			resultList.addAll(root.value);
			dfsTST(root.lowerTTN, depth, max, resultList);
			dfsTST(root.higherTTN, depth, max, resultList);
			dfsTST(root.currentTTN, depth - 1, max, resultList);
			
			resultList = resultList.subList(0, Math.min(max, resultList.size()));
		}
		
		return;
	}
	
	/**
	 * 借助两个Queue来广度优先遍历一棵TernaryTree
	 * @param root 根节点
	 * @param depth 遍历深度
	 * @param max 结果集大小
	 * @return
	 */
	public static List<String> bfsTST(TernaryTreeNode root, int depth, int max) {
		
		if(depth > 0 && root != null) {
			
			List<String> resultList = new LinkedList<String>();
			Queue<TernaryTreeNode> currentQueue = new LinkedList<>();
			Queue<TernaryTreeNode> nextQueue = new LinkedList<>();
			currentQueue.add(root);
			while(resultList.size() < max) {
				
				if(CollectionUtils.isEmpty(currentQueue)) {
					
					if(CollectionUtils.isEmpty(nextQueue)) {
						
						break;
					} else {
						
						currentQueue = nextQueue;
						nextQueue = new LinkedList<>();
						if(--depth < 0) {
							
							break;
						}
						continue;
					}
				} else {
					
					TernaryTreeNode currentNode = currentQueue.poll();
					resultList.addAll(currentNode.value);
					if(currentNode.lowerTTN != null) {
						
						currentQueue.add(currentNode.lowerTTN);
					}
					if(currentNode.currentTTN != null) {
						
						nextQueue.add(currentNode.currentTTN);
					}
					if(currentNode.higherTTN != null) {
						
						currentQueue.add(currentNode.higherTTN);
					}
				}
			}
			
			return resultList.subList(0, Math.min(max, resultList.size()));
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