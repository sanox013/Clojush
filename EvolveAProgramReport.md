**title: "Evolve a program!"
author: "Hu, Shinny, Sano, Ai, & Xiao, Yuting"
date: "December 15, 2016"**

<h4>**The description of our problem and why we chose it**</h4>
Given a list of non-negative integers, the program should arrange them such that they form the largest possible
number. For example, given [50, 2, 1, 9], the program should return “95021”. This problem was taken from the
website titled “Five programming problems every Software Engineer should be able to solve in less than 1 hour ”
[link to the website](http://www.shiftedup.com/2015/05/07/five-programming-problems-every-software-engineer-should-be-able-to-solve-in-less-than-1-hour).

We found this problem interesting and chose it for our project due to the following reasons: The problem is not
too simple but not too complicated, either; at a first glance it appears that this problem can be easily solved
only by lexical comparison, but it actually requires more complex solution, which will be discussed in the next
section. Additionally this problem deals with permutations and combinations, which are important aspects to many
advanced problems.

<h4>**How we set up the problem**</h4>
For “input-set”, at first, we fed the program a set of lists of positive integers, where each list differed in
length (e.g., some lists contained four integers and others three integers); but it turned out that it made it
more difficult for us to solve our problem. Therefore, we decided to “standardize” our inputs by using a set of
lists, all of which were the same length (e.g., all the input lists contained three integers).   

Our “expected-output” function was written based on the solution proposed by the author of the website. The
function sorts a list of integers in a descending order with a comparator that compares two integers (X and Y)
based upon the concatenated values of these integers; “X||Y” and “Y||X”, which represent X and Y respectively.
For example, when comparing two integers 50 and 9, “509” (representing 50) and “950” (representing 9) are compared;
and “950” is larger than “509”, therefore, 9 is considered a larger value than 50 for a sorting purpose. The elements
 in the list are then sorted in a way that makes up the largest value when reading from the left to right and returned
 as an integer (e.g., [50, 2, 1, 9] ⇒ [9, 50, 2, 1] ⇒ 95021).

For “evolution” function, we started by giving the program full access to all the instructions for string, integer,
boolean and execute codes. However, it turned out to be too wide a selection range. In addition, we also tried using
only a few instructions for string, integer, boolean and execute_if function.


<h4>**Results**</h4>
* When providing our program with all possible functions for string, integer, boolean and execution, we failed after
one thousand generations, and we could not see any improvement during “evolution” process. Instead, we found that the
first few generations would use solutions with a long list of functions, then it suddenly got cut down to only a few
instructions for each try till the end of one thousandth generation. This result might be due to the possibility that our
program could not find any breakthrough and just decided to simplify the results.  
* Later on, we tried using only a few functions instead; for example, `exec_if, integer_lt`, `integer_gt`, and `string_concat`.
Unfortunately, our result didn’t get better after this change.  
* In order to simplify our problem furthermore and just to test the program, we changed our input sets into lists of
three positive integers, all of which were two digits (e.g., [60, 10, 30], instead of [87, 123, 3]). This change made
little difference to our results; so we reduced the number of integers even more and had two integers of two digits in
each input list. After this change, the program generated the expected numbers successfully.
