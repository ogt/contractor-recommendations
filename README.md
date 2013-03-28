machine-learning
================
[![Build Status](https://secure.travis-ci.org/ogt/contractor-recommendations.png?branch=master)](http://travis-ci.org/ogt/contractor-recommendations)

A simple example of using machine learning algorithms to make recommendations of contractors based 
on their past success/failures being hired for a particular jobs. Uses liblinear.

To run locally
```
> mvn clean install
> java -cp 'target/classes:target/dependency/*' ContractorRecommendationsTraining
```
This will create/update a few files in the `data/test/output` folder. 

Thats all.
