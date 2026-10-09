package ca.sheridancollege.aljanmus;



/**deliveryy**/ 
public class CoolLinkedList {

	private static class Node {
		String payload;
		Node nextNode;

		Node(String payload) {
			this.payload = payload;
			this.nextNode = null;
		}
	}

	private Node head;
	private int count;

	public CoolLinkedList() {
		head = null;
		count = 0;
	}

	public int size() {
		return count;
	}

	public void addToFront(String value) {
		Node newNode = new Node(value);
		newNode.nextNode = head;
		head = newNode;

		count++;
	}

	public void addToEnd(String value) {
		Node newNode = new Node(value);

		if (head == null) {
			head = newNode;
		} else {
			Node current = head;
			while (current.nextNode != null) {
				current = current.nextNode;
			}
			current.nextNode = newNode;
		}

		count++;
	}

	public String getIndex(int index) {
		if (index < 0 || index >= count) {
			return null;
		}

		Node current = head;
		for (int i = 0; i < index; i++) {
			current = current.nextNode;
		}
		return current.payload;
	}

	@Override
	public String toString() {
		String result = "";
		Node current = head;

		while (current != null) {
			result += current.payload;
			current = current.nextNode;
		}
		return result;
	}

	public static void main(String[] args) {
		CoolLinkedList list = new CoolLinkedList();

		list.addToFront("C");
		list.addToEnd("A");
		list.addToFront("B");
		list.addToEnd("D");

		System.out.println("List: " + list);
		System.out.println("Size: " + list.size());
		System.out.println("Index 0: " + list.getIndex(0));
		System.out.println("Index 1: " + list.getIndex(1));
		System.out.println("Index 2: " + list.getIndex(2));
		System.out.println("Index 3: " + list.getIndex(3));
		System.out.println("Index 4: " + list.getIndex(4));
	}
}