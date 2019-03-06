package se.yolean.kafka.topicscopy;

import java.util.Scanner;

public class Exempel {

  public static void main(String[] args) {
    // create a scanner so we can read the command-line input
    Scanner scanner = new Scanner(System.in);

    //  prompt for the user's name
    System.out.print("Enter your name: ");

    // get their input as a String
    String username = scanner.next();

    // prompt for their age
    System.out.print("Enter your age: ");

    // get the age as an int
    int age = scanner.nextInt();

    System.out.println(String.format("%s, your age is %d", username, age));
  }

}
