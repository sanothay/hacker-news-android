package com.dudev.android.hackernews.Util;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ListUtils {

  private ListUtils() {}

  @SuppressWarnings("unchecked")
  public static <T> void removeDuplicate(List <T> list) {
    Set <T> set = new HashSet <T>();
    List <T> newList = new ArrayList <T>();
    for (Iterator <T>iter = list.iterator();    iter.hasNext(); ) {
       Object element = iter.next();
       if (set.add((T) element))
          newList.add((T) element);
       }
       list.clear();
       list.addAll(newList);
    }

  public static void main(String[] args) {
    ArrayList<String> list = new ArrayList<String>();
    list.add("Bart");
    list.add("Lisa");
    list.add("Marge");
    list.add("Marge");
    list.add("Barney");
    list.add("Homer");
    list.add("Maggie");
    System.out.println(list);
    // output : [Bart, Lisa, Marge, Marge, Barney, Homer, Maggie]
    ListUtils.removeDuplicate(list);
    System.out.println(list);
    // output : [Bart, Lisa, Marge, Barney, Homer, Maggie]
  }
}