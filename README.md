machine-learning
================
{<img src="https://travis-ci.org/sanigo/contractor-recommendations.png" />}[https://travis-ci.org/sanigo/contractor-recommendations.png]

A simple example of using machine learning algorithms to make recommendations of contractors based 
on their past success/failures being hired for a particular jobs. Uses liblinear.

To run locally
```
> mvn clean install
> java -cp 'target/classes:target/dependency/*' ContractorRecommendationsTraining
```
This will create/update a few files in the `data/test/output` folder. 
Thats all.
