package com.sdww8591.utils.algorithm.tree;

/**
 * 红黑树是二叉平衡树的典型实现方式，许多数据结构都有应用，具体原理请参见{@link https://www.jianshu.com/p/37c845a5add6}
 * @param <K>
 * @param <V>
 */
public class RedBlackTree<K extends Comparable, V> {

    private static final Boolean BLACK = false;

    private static final Boolean RED = true;

    private Node<K, V> root = null;

    private class Node<K, V> {

        public K key;

        public V value;

        public Boolean color;

        public Node<K, V> left;

        public Node<K, V> right;

        public Node(K key, V value, Boolean color) {
            this.key = key;
            this.value = value;
            this.color = color;
        }
    }

    /**
     * 红黑树的null节点也视为BLACK
     * @param node
     * @return
     */
    private Boolean isRed(Node<K, V> node) {
        if(node == null) return BLACK;
        return node.color;
    }

    /**
     * 左旋操作
     * 对于典型A有一个红色右Node B的情况，转换为B为root, 有一个左NodeA，如下图：
     * A          B
     *  \   ->   /
     *   B      A
     * @param node
     * @return
     */
    private Node<K, V> rotateLeft(Node<K, V> node) {
        /*if(root == null
                || root.right == null
                || root.right.color == BLACK) {
            throw new IllegalStateException();
        }*/

        Node<K, V> newNode = node.right;
        node.right = newNode.left;
        newNode.left = node;

        newNode.color = node.color;
        node.color = RED;
        return newNode;
    }

    /**
     * 右旋操作，与左旋相反
     * 对于典型A有一个红色左Node B的情况，转换为B为root, 有一个右Node A, 如下图：
     *    A        B
     *   /    ->    \
     *  B            A
     * @param node
     * @return
     */
    private Node<K, V> rotateRight(Node<K, V> node) {
        /*if(root == null
                || root.left == null
                || root.left.color == BLACK) {
            throw new IllegalStateException();
        }*/

        Node<K, V> newNode = node.left;
        node.left = newNode.right;
        newNode.right = node;

        newNode.color = node.color;
        node.color = RED;
        return newNode;
    }

    /**
     * 颜色反转：如果一个Node的左右两个子Node颜色一致，则反转其颜色
     * @param node
     */
    private void colorFlip(Node<K, V> node) {
        /*if(node.left == null
                || node.left.color == BLACK
                || node.right == null
                || node.right.color == BLACK) {
            throw new IllegalStateException();
        }*/

        node.left.color = !node.left.color;
        node.right.color = !node.right.color;
        node.color = !node.color;
    }

    /**
     * 红黑树的插入操作，返回结果为左倾红黑树
     * @param node
     * @param key
     * @param value
     * @return
     */
    public Node<K, V> insert(Node<K, V> node, K key, V value) {
        //如果当前节点不存在
        if(node == null) {
            return new Node<>(key, value, RED);
        }

        //找到了key相等的节点
        int com = node.key.compareTo(key);
        if(com == 0) {
            node.value = value;
            return node;
        }

        //colorFlip，相当于一个4-Node分裂为两层，中间节点向上传递一层，剩余两个2-Node
        if(isRed(node.left) && isRed(node.right)) {
            colorFlip(node);
        }

        //递归插入
        if(com < 0) {
            node.left = insert(node.left, key, value);
        } else {
            node.right = insert(node.right, key, value);
        }

        //由于本例为左倾红黑树实现，因此遇到右倾的情况都需要需要转为左倾
        if(isRed(node.right)) {
            node = rotateLeft(node);
        }

        //连续两个左倾的情况，需要将其转为一个4-node，flip等下次插入时再做
        if(isRed(node.left) && node.left != null && isRed(node.left.left)) {
            node = rotateRight(node);
        }
        return node;
    }

    /**
     * 核心思路：为了保证红黑树的平衡性，则必须保证删除的节点为Red类型，遇到BLACK Node则需要想办法将其处理为RED Node
     * 以删除红黑树最大节点为例：
     * case 1：max Node 的color为RED，直接删除即可
     * case 2: max Node 的color为BLACK
     *  case 2-1: 在case 2 的基础上，max Node的兄弟节点为2-Node，此时只需要反向做一次colorflip即可（相当于max Node， max Node的兄弟， 以及max Node的直接parent共同组成了一个4-node）
     *  case 2-2: 在case 2 的基础上，max Node的兄弟节点不为2-Node，此时需要将max Node，max Node的直接parent， 以及max Node的兄弟节点的最大值重新组合其结构，使得最后一个Node为3-node
     *  总结：重新组合max Node、 max Node的parent、以及max Node 兄弟的最大值之间的关系。
     * 除了删除之外，将删除后的结果处理回左倾红黑树也是其中关键之一
     */
    private void deleteMax() {
        root = deleteMaxNode(root);
        root.color = BLACK;
    }

    private Node<K, V> deleteMaxNode(Node<K, V> node) {
        if(isRed(node.left)) {
            node = rotateRight(node);
        }
        //递归结束条件
        //TODO:为啥这里只判断node.right，而不用判断node.left
        if(node.right == null && node.left == null) {
            return null;
        }
        //当前节点的右节点是自己的子节点，且子节点的颜色为BLACK
        if(!isRed(node.right) && !isRed(node.right.left)) {
            node = moveRedRight(node);
        }
        node.right = deleteMaxNode(node.right);
        fix(node);
        return node;
    }

    private Node<K, V> moveRedRight(Node<K, V> node) {
        colorFlip(node);
        if(isRed(node.left.left)) {
            node = rotateRight(node);
            colorFlip(node);
        }
        return node;
    }

    private void fix(Node<K, V> node) {
        if(isRed(node.right)) {
            node = rotateLeft(node);
        }
        if(isRed(node.left) && isRed(node.left.left)) {
            node = rotateRight(node);
        }
        if(isRed(node.left) && isRed(node.right)) {
            colorFlip(node);
        }
    }
}
