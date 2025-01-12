/*
Author: Seongyun Lim
Email: slim2020@my.fit.edu
Course: CSE2010
Section: Section 03
Description of this file: recommend songs to a target customer based on other similar taste customers.
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;


// all other parts are the same to the HW4 except buildHeap method and heapifying the initial heap in main method
class CusDistance2 implements Comparable<CusDistance2> {
    private double distance;
    private String name;
    private int[] ratings;

    public CusDistance2(double distance, String name, int[] ratings) {
        this.distance = distance;
        this.name = name;
        this.ratings = ratings;
    }

    public String name() {
        return this.name;
    }
    public double distance() {
        return this.distance;
    }
    public int[] ratings(){ return this.ratings; }
    public void setDistance(double distance) { this.distance = distance; }

    @Override
    public int compareTo(CusDistance2 second) {
        if (this.distance() < second.distance()) {
            return -1;
        } else if (this.distance() == second.distance()) {
            return this.name().compareTo(second.name());
        } else return 1;
    }
}

class MinHeap2 {

    private ArrayList<CusDistance2> list;

    public MinHeap2() {
        list = new ArrayList<>();
    }

    protected void swap(int i, int j){
        CusDistance2 temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
    protected int parent(int n) { return (n-1)/2; }
    protected int left(int n) {
        return n*2+1;
    }
    protected int right(int n){
        return n*2+2;
    }

    protected boolean hasLeft(int n) {
        return left(n) < list.size();
    }
    protected boolean hasRight(int n) {
        return right(n) < list.size();
    }

    protected void downHeap(int n) {
        while (hasLeft(n)) {
            int leftIdx = left(n);
            int minIdx = left(n);
            if (hasRight(n)){
                int rightIdx = right(n);
                if (list.get(rightIdx).compareTo(list.get(leftIdx)) < 0){
                    minIdx = rightIdx;
                }
            }
            if (list.get(minIdx).compareTo(list.get(n)) < 0) {
                swap(minIdx, n);
                n = minIdx;
            } else break;
        }
    }

    // bottom up heap construction
    // down heap from the last internal node index
    public void buildHeap() {
        for (int i = (list.size()/2) - 1; i >= 0; i--) {
            downHeap(i);
        }
    }

    public void upHeap(int n) {
        while (n > 0) {
            int p = parent(n);
            if (list.get(n).compareTo(list.get(p)) < 0) {
                swap(n, p);
                n = p;
            } else break;
        }
    }

    public void insert(CusDistance2 cus) {
        list.add(cus);
        upHeap(list.size()-1);
    }

    public CusDistance2 removeMin(){
        if (list.size() == 1) {
            return list.remove(0);
        } else {
            CusDistance2 min = list.get(0);
            list.set(0, list.remove(list.size()-1));
            downHeap(0);
            return min;
        }
    }
    public void addElement(CusDistance2 cus) {
        this.list.add(cus);
    }

    // get the min distance value
    public CusDistance2 getMin(){
        if (list == null) return null;
        return list.get(0);
    }


    public boolean isEmpty(){
        return list.size()==0;
    }
}

public class RecommendationSystemEnhanced {
    public static void main(String[] args) throws FileNotFoundException {
        File file = new File(args[0]);
        Scanner sc = new Scanner(file);
        String targetName = sc.nextLine();
        ArrayList<CusDistance2> customerList = new ArrayList<>();
        CusDistance2 targetCus = null;
        while (sc.hasNext()){
            int[] arr = new int[10];
            String[] temp = sc.nextLine().split(" ");
            for (int i = 0; i < 10; i++) {
                arr[i] = Integer.parseInt(temp[i+1]);
            }
            CusDistance2 current = new CusDistance2(0, temp[0], arr);
            if (temp[0].equals(targetName)) {
                targetCus = current;
            } else {
                customerList.add(current);
            }
        }
        Collections.sort(customerList);
        MinHeap2 heap = new MinHeap2();
        for (int i = 0; i < customerList.size(); i++) {
            double distance = calculate(targetCus.ratings(), customerList.get(i).ratings());
            customerList.get(i).setDistance(distance);
            if (distance == -1) continue;
            heap.addElement(customerList.get(i));
        }
        heap.buildHeap(); // bottom up heap construction
        file = new File(args[1]);
        sc = new Scanner(file);
        while (sc.hasNext()){
            String[] input = sc.nextLine().split(" ");
            switch(input[0]) {
                case "AddCustomer":
                    System.out.println(String.join(" ", input));
                    addCus(input, customerList, targetCus, heap);
                    break;
                case "RecommendSongs":
                    recommend(heap, targetCus);
                    break;
                case "PrintCustomerDistanceRatings":
                    System.out.println("PrintCustomerDistanceRatings");
                    System.out.printf("%6s", " ");
                    System.out.printf("%-11s", targetCus.name());
                    for (int i = 0; i < 10; i++) {
                        System.out.print(targetCus.ratings()[i] + " ");
                    }
                    System.out.println();
                    print(customerList);
                    break;
                default:
                    System.out.println("wrong query");
            }
        }
    }

    public static double calculate(int[] target, int[] other){
        double cnt = 0;
        double ratingSum = 0;
        for (int i = 0; i < 10; i++) {
            if (target[i] != 0 && other[i] != 0) {
                cnt += 1;
                ratingSum += Math.abs(target[i]-other[i]);
            }
        }
        if (cnt == 0) return -1;
        double distance = 1/cnt + ratingSum/cnt;
        return distance;
    }

    public static void addCus(String[] input, ArrayList<CusDistance2> list, CusDistance2 target, MinHeap2 heap) {
       String name = input[1];
       int[] rating = new int[10];
       for (int i = 0; i < 10; i++) {
           rating[i] = Integer.parseInt(input[i+2]);
       }
       double distance = calculate(target.ratings(), rating);
       CusDistance2 temp = new CusDistance2(distance, name, rating);
       list.add(temp);
       Collections.sort(list, new Comparator<CusDistance2>() {
           @Override
           public int compare(CusDistance2 o1, CusDistance2 o2) {
               return o1.name().compareTo(o2.name());
           }
       });
       if (distance != -1) heap.insert(temp);
    }

    public static void recommend(MinHeap2 heap, CusDistance2 target) {
        boolean find = false;
        int[] song = new int[10];
        CusDistance2 temp = null;
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
        if (!find) sb.append("none");
        else {
            sb.append(temp.name() + " ");
            for (int i = 1; i < 11; i++) {
                if (song[i-1] == 1) {
                    sb.append("song" + i + " ");
                    sb.append(temp.ratings()[i-1] + " ");
                }
            }
        }
        System.out.println(sb);
    }

    public static void print(ArrayList<CusDistance2> list) {
        for (int i = 0; i < list.size(); i++) {
            CusDistance2 temp = list.get(i);
            if (temp.distance() == -1) {
                System.out.printf("%-6s", "-----");
            } else {
                System.out.printf("%-6.3f", temp.distance());
            }
            System.out.printf("%-11s", temp.name());
            for (int j = 0; j < 10; j++) {
                System.out.print(temp.ratings()[j] + " ");
            }
            System.out.println();
        }
    }
}
