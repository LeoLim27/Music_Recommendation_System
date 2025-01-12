/**
 * Author: Seongyun Lim
 * Email: slim222@wisc.edu
 * Description of this file : recommend music for a customer based on another customer with the most similar taste.
 *
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

// CusDistance object storing customer's distance, name, and ratings
class CusDistance implements Comparable<CusDistance> {

    private double distance;
    private String name;
    private int[] ratings;

    // constructor
    public CusDistance(double distance, String name, int[] ratings) {
        this.distance = distance;
        this.name = name;
        this.ratings = ratings;
    }

    // getter method
    public String name() {
        return this.name;
    }
    public double distance() {
        return this.distance;
    }
    public int[] ratings(){ return this.ratings; }
    // setter method
    public void setDistance(double distance) { this.distance = distance; }

    // enable cusDistance objects to be sorted based on the distance and name if distances are the same.
    @Override
    public int compareTo(CusDistance second) {
        if (this.distance() < second.distance()) { // -1 if this object distance is smaller than second one
            return -1;
        } else if (this.distance() == second.distance()) { // compare the name if distances are the same
            return this.name().compareTo(second.name());
        } else return 1; // return positive num if this distance is larger than second one
    }
}

// priority queue implementation
class MinHeap {

    ArrayList<CusDistance> list; // array based min heap

    public MinHeap() {
        list = new ArrayList<>();
    } // constructor initializing heap

    // method for swapping two elements in an array
    protected void swap(int i, int j){
        CusDistance temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    protected int parent(int n) {
        return (n - 1) / 2;
    } // element's parent index
    protected int left(int n) {
        return n*2+1;
    } // element's left child index
    protected int right(int n){
        return n*2+2;
    } // element's right child index
    protected boolean hasLeft(int n) { return left(n) < list.size(); } // check if it has left child
    protected boolean hasRight(int n) {
        return right(n) < list.size();
    } // check if it has right child

    // upHeap the element in the position n
    protected void upHeap(int n) {
        // 0 is the root node index, so do upHeap before reaching to the root.
        while (n > 0) {
            int p = parent(n);
            // do swap if current object is smaller than a parent object
            if (list.get(n).compareTo(list.get(p)) < 0) {
                swap(n, p);
                n = p;
            } else break; // stop if current is smaller than parent
        }
    }

    // downHeap the object in a position n
    protected void downHeap(int n) {
        // do down heap if it has child node.
        while (hasLeft(n)) {
            int leftIdx = left(n);
            int minIdx = left(n); // store the minimum's index among left and right.
            // update minIdx if it has right child and this is smaller than left child
            if (hasRight(n)){
                int rightIdx = right(n);
                if (list.get(rightIdx).compareTo(list.get(leftIdx)) < 0){
                    minIdx = rightIdx;
                }
            }
            // swap if child is smaller than current or else break
            if (list.get(minIdx).compareTo(list.get(n)) < 0) {
                swap(minIdx, n);
                n = minIdx;
            } else break;
        }
    }

    // insert an object into a minHeap and place it into the right place.
    public void insert(CusDistance cus) {
        list.add(cus);
        upHeap(list.size()-1);
    }

    // remove the min value from the heap
    public CusDistance removeMin(){
        if (list.isEmpty()) return null;
        // just remove and return the object if heap has only one object
        if (list.size() == 1) {
            return list.remove(0);
        }
        // update the minHeap after get the min object
        CusDistance temp = list.get(0);
        list.set(0, list.remove(list.size()-1));
        downHeap(0);
        return temp;
    }

    // get the min distance value
    public CusDistance getMin(){
        if (list == null) return null;
        return list.get(0);
    }

    // check if minHeap is empty
    public boolean isEmpty(){
        return list.size()==0;
    }
}

public class RecommendationSystem {
    public static void main(final String[] args) throws FileNotFoundException {
        File file = new File(args[0]); // get ratings file
        Scanner sc = new Scanner(file); // read ratings file
        final String targetName = sc.nextLine(); // target customer's name at the first line
        // CustomerList stores CusDistance object and targetCus stores the target Customer object
        final ArrayList<CusDistance> customerList = new ArrayList<>();
        CusDistance targetCus = null;
        while (sc.hasNext()){
            int[] arr = new int[10]; // current customer's ratings
            String[] temp = sc.nextLine().split(" "); // store the input
            // update the ratings array
            for (int i = 0; i < 10; i++) {
                arr[i] = Integer.parseInt(temp[i+1]);
            }
            CusDistance current = new CusDistance(0, temp[0], arr); // create current CusDistance object
            if (temp[0].equals(targetName)) { // store into the targetCus if the target
                targetCus = current;
            } else {
                customerList.add(current); // store into the list if not the target
            }
        }
        Collections.sort(customerList); // sort the customer list in alphabetical order (all distances == 0)
        final MinHeap heap = new MinHeap(); // initialize priority queue
        // add CusDistance object into a heap if the distance can be calculated.
        for (int i = 0; i < customerList.size(); i++) {
            double distance = calculate(targetCus.ratings(), customerList.get(i).ratings()); // calculate distance
            customerList.get(i).setDistance(distance); // set the distance of CusDistance object in the customerList.
            if (distance == -1) continue; // no add in Heap if distance is not measurable
            heap.insert(customerList.get(i)); // add into Heap if distance is measurable
        }
        file = new File(args[1]); // get actions file
        sc = new Scanner(file); // read actions file
        while (sc.hasNext()){
            String[] input = sc.nextLine().split(" "); // store the input into an array
            switch(input[0]) {
                case "AddCustomer": // AddCustomer request
                    System.out.println(String.join(" ", input)); // output the request itself
                    addCus(input, customerList, targetCus, heap); // update the customerList and Heap
                    break;
                case "RecommendSongs": // RecommendSongs request
                    recommend(heap, targetCus);
                    break;
                case "PrintCustomerDistanceRatings": // PrintCustomer request
                    System.out.println("PrintCustomerDistanceRatings");
                    // printing the target customer's information first
                    System.out.printf("%-6s", " "); // white space for distance
                    System.out.printf("%-11s", targetCus.name()); // left aligned name in 11 space
                    for (int i = 0; i < 10; i++) { // printing the ratings information
                        System.out.print(targetCus.ratings()[i] + " ");
                    }
                    System.out.println();
                    // print the customerList
                    print(customerList);
                    break;
                default:
                    System.out.println("wrong query");
            }
        }
    }

    // calculate the distance based on two ratings
    public static double calculate(int[] target, int[] other){
        double cnt = 0;
        double ratingSum = 0;
        for (int i = 0; i < 10; i++) {
            // only consider the song both target and other rated
            if (target[i] != 0 && other[i] != 0) {
                cnt += 1; // increment count
                ratingSum += Math.abs(target[i]-other[i]); // add the difference into ratingSum
            }
        }
        if (cnt == 0) return -1; // return -1 if distance cannot be calculated (no common rated song)
        double distance = 1/cnt + ratingSum/cnt;
        return distance; // return distance
    }

    // method for add customer request
    public static void addCus(String[] input, ArrayList<CusDistance> list, CusDistance target, MinHeap heap) {
       String name = input[1]; // new customer's name
       int[] rating = new int[10]; // new customer's ratings
       for (int i = 0; i < 10; i++) { // update the ratings info
           rating[i] = Integer.parseInt(input[i+2]);
       }
       double distance = calculate(target.ratings(), rating); //calculate the distance from target customer
       CusDistance temp = new CusDistance(distance, name, rating); // create new CusDistance object
       list.add(temp); // add to customerList and sort the list again
       Collections.sort(list, new Comparator<CusDistance>() {
           @Override // comparator to sort the customerList based on the name, not distance
           public int compare(CusDistance o1, CusDistance o2) {
               return o1.name().compareTo(o2.name());
           }
       });
       if (distance != -1) heap.insert(temp); // add to heap if the distance can be calculated
    }

    // print output for recommend song request; use heap and target to find the song
    public static void recommend(MinHeap heap, CusDistance target) {
        boolean find = false;
        int[] song = new int[10]; // song array to see which song is recommended
        CusDistance temp = null;
        while (!find && !heap.isEmpty()) {
            temp = heap.getMin();
            for (int i = 0; i < 10; i++) {
                if (target.ratings()[i] == 0 && temp.ratings()[i] >= 4) {
                    find = true;
                    song[i] = 1;
                }
            }
            if (!find) heap.removeMin();
        }
        StringBuilder sb = new StringBuilder("RecommendSongs ");
        if (!find) sb.append("none"); // no recommend song
        else { // yes recommend song
            sb.append(temp.name() + " ");
            for (int i = 1; i < 11; i++) { // print the song number and closest customer's rating
                if (song[i-1] == 1) {
                    sb.append("song" + i + " ");
                    sb.append(temp.ratings()[i-1] + " ");
                }
            }
        }
        System.out.println(sb);
    }

    // print the customerList table
    public static void print(ArrayList<CusDistance> list) {
        // follow the format and spacing using formatter
        for (int i = 0; i < list.size(); i++) {
            CusDistance temp = list.get(i);
            if (temp.distance() == -1) { // ----- when distance cannot be calculated
                System.out.printf("%-6s", "-----");
            } else { // left aligned distance with three decimal places in 6 spaces
                System.out.printf("%-6.3f", temp.distance());
            }
            System.out.printf("%-11s", temp.name()); // left aligned name with 11 space
            for (int j = 0; j < 10; j++) { // print the ratings information
                System.out.print(temp.ratings()[j] + " ");
            }
            System.out.println();
        }
    }
}
