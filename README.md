machine-learning
================

Data products's machine learning engine.

Assuming you have java/git[hub]/maven, to run locally
```
> hub clone ogt/contractor-recommendation
> cd !$
> mvn clean install
> java -cp 'target/classes:target/dependency/*' ContractorRecommendationsTraining
```
This will create/update a few files in the data/test/output folder. 
Thats all.
