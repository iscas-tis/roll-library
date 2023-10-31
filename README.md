# roll-library
Regular Omega Language Learning Library


ROLL is a library of learning algorithms for ω-regular languages. It consists of all the learning algorithms for the complete class of ω-regular languages available in the literature, namely
  - the learning algorithms for FDFAs
     * the algorithm in [5] learns three canonical FDFAs using observation tables, which is based on results in [3],
     * the algorithm in [6] learns three canonical FDFAs using classification trees;
  - and the learning algorithms for Büchi automata
     * the algorithm in [4] learns a Büchi automaton by combining L<sup>*</sup> algorithm [1] and results in [2],
     * the algorithm in [6] learns the Büchi automata via learning three canonical FDFAs.
     * the algorithm in [10] learns the limit-deterministic Büchi automata via learning three canonical FDFAs.

Since 2023, it also added the support for the limit FDFAs proposed in [11] and the following:
  - the learning algorithms for limit FDFAs using observation tables or classification trees;
  - the learning algorithms for Büchi automata via learning limit FDFAs;
  - and the learning algorithms for transition-based deterministic Büchi automata for an unknown DBA language based on learning limit FDFAs;


The ROLL library is implemented in JAVA. Its DFA operations are delegated to the [dk.brics.automaton](http://www.brics.dk/automaton/) package. We use [RABIT](http://www.languageinclusion.org/doku.php?id=tools) tool to check the equivalence of
two Büchi automata. 

----

[1] Dana Angluin. "Learning regular sets from queries and counterexamples." Information and computation 75.2 (1987): 87-106.


[2]  Hugues Calbrix, Maurice Nivat, and Andreas Podelski. "Ultimately periodic words of rational ω-languages." In MFPS. Springer Berlin Heidelberg, 1993: 554-566.

[3] Oded Maler and Ludwig Staiger. "On syntactic congruences for ω-languages." In STACS. Springer Berlin Heidelberg, 1993: 586-594.

[4] Azadeh Farzan, Yu-Fang Chen, Edmund M. Clarke, Yih-Kuen Tsay, Bow-Yaw Wang. "Extending automated compositional verification to the full class of omega-regular languages." In TACAS. Springer Berlin Heidelberg, 2008: 2-17.

[5] Dana Angluin, and Dana Fisman. "Learning regular omega languages." In ALT. Springer International Publishing, 2014: 125-139.

[6] Yong Li, Yu-Fang Chen, Lijun Zhang, and Depeng Liu. "A Novel Learning Algorithm for Büchi Automata based on Family of DFAs and Classification Trees." In TACAS. Springer Berlin Heidelberg, 2017: 208-226. [paper](https://arxiv.org/abs/1610.07380)

[7] Yong Li, Andrea Turrini, Lijun Zhang and Sven Schewe. "Learning to Complement Büchi Automata." In VMCAI. Springer, Cham, 2018:313-335.

[8] Radu Grosu, Scott A. Smolka. "Monte carlo model checking." In TACAS. Springer-Verlag Berlin, Heidelberg, 2005:271-286.

[9] Yong Li, Andrea Turrini, Xuechao Sun and Lijun Zhang. "Proving Non-inclusion of Büchi Automata Based on Monte Carlo Sampling." In ATVA. Springer, 2020: 467-483.


[10] Yong Li, Yu-Fang Chen, Lijun Zhang, and Depeng Liu. "A Novel Learning Algorithm for Büchi Automata based on Family of DFAs and Classification Trees." In I&C. (Added an algorithm to transform an FDFA to a limit-deterministic Büchi automaton) [paper](https://tis.ios.ac.cn/roll/lib/exe/fetch.php?media=iandc.pdf)

[11] Yong Li, Sven Schewe, and Qiyi Tang. "A Novel Family of Finite Automata for Recognizing and Learning ω-Regular Languages." In ATVA 2023. [paper](https://arxiv.org/abs/2307.07490)

For more information, please visit our website http://iscasmc.ios.ac.cn/roll/.
