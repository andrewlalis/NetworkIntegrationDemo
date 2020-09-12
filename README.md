# Network Integration Demo
A relatively simple application, which doesn't accomplish anything useful, but does serve as the basis for illustrating how one would write integration tests for their network components.

## Application Description
The application consists of a central server, which we'll call the **Delegator**, and some clients that come in two varieties, **Producers** and **Consumers**. It works like this:

* **Producers** will generate random natural numbers and send them to the **Delegator**.
* The **Delegator** receives these numbers, and sends them to an eligible **Consumer**.

Any number of producers can simply connect to the delegator without any setup and begin transmitting numbers, but consumers need to give the delegator their 'preference' for which numbers they want to receive. The consumer gives the delegator a single number, and if the delegator receives a number from a producer which is a multiple of that number, the delegator will send the number to the consumer.

For example, let's say a producer generates the number 42 and sends it to the delegator. Now there just so happens to be a consumer registered with the number 6, and since 42 is a multiple of 6, the delegator sends this number to that consumer.

## Testing Strategy
While of course it is possible to unit test each part of this application individual, and, for example, ensure that producers can correctly generate and send numbers into a socket's byte buffer, this can become tedious and impractical. Not only would we have to redesign our system to accommodate the ability to inject 'mocked' sockets, but we can't be 100% certain that the application will work when deployed, unless we test everything together.


