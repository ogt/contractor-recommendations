machine-learning
================

A simple example of using machine learning algorithms to make recommendations of contractors based 
on their past success/failures being hired for a particular jobs

Assuming you have java/git[hub]/maven, to run locally
```
> hub clone ogt/contractor-recommendation
> cd !$
> mvn clean install
> java -cp 'target/classes:target/dependency/*' ContractorRecommendationsTraining
```
This will create/update a few files in the data/test/output folder. 
Thats all.
