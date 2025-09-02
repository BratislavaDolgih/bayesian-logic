# A full description of my PET project on calculating probabilities using Bayesian logic.
## Introduction, preface.
I welcome everyone who decided to visit my repository, my GitHub page (I'll say right away that I can "swim" in some definitions) with my first pet project.

Today I suggest that you pay attention to my educational project on calculating the probabilities of events, according to Bayesian logic. This topic seemed interesting to me in implementation also because I started writing it back in early August 2025. At that time, I went through some stages of self-searching, coping with experiences and everything in this spirit.

> 🤝 The idea was suggested to me by ChatGPT, who during our conversation on a spiritual topic mentioned that he could try to show and calculate the probabilities of one personal event based on the hypotheses and facts. I immediately asked him to explain what he meant, and I was incredibly surprised to learn that.

## Who was the project written for?
First of all, for those who want to verify the occurrence of an event based on facts. Those who want to clarify a certain situation and once again make sure/refuse this or that idea.
And secondly, this educational project helped me in mastering the practice of the Java language.

## Technical part, features and means for implementation.
I implemented my project on a very interesting topic in the Java programming language, because now it is my favorite language, in which I feel comfortable thinking and programming.

>🛠 ***Versions details (info from PowerShell):***
> - **Java Compiler Version:** *javac 23.0.2*
> - **Java Version:** *java version "23.0.2" 2025-01-21*
> - **IDE:** *IntellijIDEA 2025.2 Community Edition*

> **Time spent on implementation**: *1.5-2 weeks (with refactoring and adding features)*.

## A few annotations about the Bayesian method, its meaning and purpose.

### **Idea.**
You have a *thesis* (the main object/statement that needs to be "estimated" how likely it is relative to some hypotheses), *hypotheses* H_1, ..., H_m (possible explanations/classes that **cover the entire probability space**, in other words, should be mutually exclusive), *facts* F_1, ..., F_n (features, obvious observations) and a *table of conditional probabilities* P(F_i | H_j).
The goal is to calculate the posterior probabilities of the hypotheses P(H_j | F_1, ..., F_n) and output the result in a readable form.
### **Main formula (Bayes).**
For once hypothesis H and set of facts F_1, ..., F_n:
***P(H | F_1, ..., F_n) = P(H) * П_i[ P(F_i | H) ] / P(F_1, ..., F_n)***, 
where denominator equals:
***P(F_1, ..., F_n) = Σ_k [ P(Hk) * Π_i P(Fi | Hk) ]***

### **Important assumptions:**
1) The project uses a _naive_ assumption: facts F_i​ are considered conditionally independent under a fixed hypothesis H_j​. This makes the probability of the population equal to the product of the individual conditional probabilities. If the facts are dependent, the results may be distorted - then a more complex model is needed.
2) If the table shows P(F_i∣H_j) = 0, then when calculating the product for this hypothesis, the situation will be that the hypothesis gets zero "weight" regardless of the other facts, even if the other features strongly support it - such behavior is usually incorrect and undesirable for numerical algorithms. **Conclusion:** in the table it is **prohibited** to put 0 as a probability. Allow only values ​​in the interval (0,1].

## Structure of packages and classes in project-pack:
```
your_name_folder\
!--logs
!--pets\bayesianlogic\
		!-- except
			!--BayesianLogicalException.java
			!--CrashException.java
			!--IncorrectFileValuesException.java
			!--NullStringPathException.java
			!--ProbabilityInputException.java
		!--legacy
			!--OldBayesianUtils.java
		!--logger
			!--BayesianFormatter.java
			!--BayesianJSONFormatter.java
			!--LogException.java
			!--LoggerForBayesian.java
		!--stuff
			!--BayesianBody.java
		
		# Other files + main(String... args), for example:
		MainPoint.java
		
		inputAllData.txt        # for readAll()
		
		test_thesis.txt         # for readThesis()
		
		test_hypotheses.txt     # for readHypotheses()
		
		test_facts.txt          # for readFacts()
		
		test_probabilities.txt  # for readProbabilities()
!--README_ru.md
!--README_en.md
```

## Compilation from PowerShell:
You should make a folder, where you will have downloaded stuff from repository, for my situation — `Bayesian`.

```
...> mkdir Bayesian
```

```
...\Bayesian > git clone ...
```

In copied repository there are few folders and two README-files:
```
!--logs\   # for logging in JSON-format.
!--out\    # for compiled classes.
!--pets\   # .java-files
!--README_ru.md
!--README_en.md
```

In PowerShell go to folder-with-copied-repository.
```
...\Bayesian\ > javac -d out pets/**/*.java
```

Run this command:
```
..\Bayesian> java -cp out pets.bayesianlogic.MainPoint
```

The output should be by default with the probability calculated by me in advance, based on the files prepared in advance.
Enjoy!

> **All methods are described in detail in Java-doc format, here I describe the concept of the educational project, nothing more!**
## Visual example of work (manually).
First, you need to define the thesis that you will work on. I will take as an example the thesis that I used as an idea for writing a project.

##### Backstory, so that it becomes clear what I am talking about:
When I saw a circle in the telegram channel of a girl I liked, where she was filming cool burning glasses in a dark atmosphere. At the beginning of the "circle" someone's hand got. Subsequently, there were no pointers to anything, except for the words at the end: "This is such a village evening."

> **Main thesis:** "Was it a man's hand or a woman's hand in the girl's video message in the Telegram channel?"

Next, we need to define mutually exclusive hypotheses and choose the most objective probability from our point of view:

> **Hypotheses:**
> *H_1* — "The hand was female"
> *H_2* — "The hand was male"
> *H_3* — "It's a village vibe, a friend's hand"

I'll explain why this choice was made. Female/male hand — it's clear. A friend's hand and a random girl's hand are different things, even though we are talking about the same "nature" of hands.

Let's define the probabilities by estimating CONDITIONALLY:

> **Hypothesis chances:**
> *H_1* = 0.4
> *H_2* = 0.4
> *H_3* = 0.2

We'll give equal chances to the male/female hand, and we'll give the friend fewer chances. At this stage, the fact that the sum of the probabilities should EXACTLY EQUAL one is fundamental. This confirms our property of "mutual exclusion".

Next, the facts that we introduce DIRECTLY from the OBVIOUS (currently from the circle in telegram). They do not necessarily have to be mutually exclusive, their task is based on, I repeat, the obvious:

>**Facts:**
>F_1 — "The hand looks rather thin, not very typical of a man"
>F_2 — "Darkness, glasses, probably really a country evening with a friend/relatives"
>F_3 — "The hand shows thin fingers"

Next, we make a table of conditional probabilities, in which we again give objective values:

|               | H_1 (female) | H_2 (male) | H_3 (girlfriend) |
| ------------- | ------------ | ---------- | ---------------- |
| P(F_1 \| H_i) | 0.8          | 0.2        | 0.6              |
| P(F_2 \| H_i) | 0.4          | 0.6        | 0.9              |
| P(F_3 \| H_i) | 0.85         | 0.15       | 0.7              |

**Brief justification:**
- F_1​ (thinness): much more likely for a woman's hand (0.8), unlikely for a man's (0.2), average for "mine/girlfriend" (0.6).
- F_2​ (country evening in the frame): the most logical for H3 (0.9), possible for H2 (0.6), less typical for H1 (0.4).
- F_3​ (thin fingers/neat nails): greatly strengthens the female hypothesis (0.85), practically excludes a rough male hand (0.15), quite likely for "mine" (0.7).

We calculate the probability weight for each hypothesis (stupid multiplication):
- For H_1: 0.80 * 0.4 * 0.85 * 0.4 (chance of hypothesis H_1) = 0.1088
- For H_2: 0.2 * 0.6 * 0.15 * 0.4 (chance of hypothesis H_2) = 0.0072
- For H_3: 0.6 * 0.9 * 0.7 * 0.2 (chance of hypothesis H_3) = 0.0756

Normalization factor (our denominator): 0.1088 + 0.0072 + 0.0756 = 0.1916.
A posteriori:
- P(H_1 | E) = 0.1088 / 0.1916 is approximately equal to 0.5676 (*56.8%*)
- P(H_2 | E) = 0.0072 / 0.1916 is approximately equal to 0.0376 (*3.8%*)
- P(H_3 | E) = 0.0756 / 0.1916 is approximately equal to 0.3946 (*39.4%*)

Rounding, we get: [57%, 4%, 39%] add up to 100%, which means,
- H_1 fulfillment = 57%
- H_2 fulfillment = 4%
- H_3 fulfillment = 39%
Voila!

## Example of data entry (method used) `MainPoint.readAll()`).

(The source files are written in Russian, for clarity, ***be careful***, replace with your own terms or translate.)
```
main_thesis;На видео-сообщении девочки в Telegram-канале была мужская рука или женская?

hypos_count;3
hypo;Рука была женской
hypo;Рука была мужской
hypo;Это деревенский вайб, рука подруги
hypo_chance;0.4
hypo_chance;0.4
hypo_chance;0.2

facts_count;3
fact;Рука выглядит довольно тоненькой, не сильно свойственна мужской
fact;Темнота, рюмки, вероятно, действительно деревенский вечер с подругой/родными
fact;Рука показывает тонкие пальцы

prob;1;1;0.8
prob;1;2;0.2
prob;1;3;0.6

prob;2;1;0.4
prob;2;2;0.6
prob;2;3;0.9

prob;3;1;0.85
prob;3;2;0.15
prob;3;3;0.7
```

If you've made it this far, I'd like to say thank you for reading, the project is not in mass production. It's all for educational purposes.
**Copying, appropriation - everything is allowed**.