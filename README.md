# ZIO Kafka Local Testing Bench

This is bare bones project that includes Confluent's all-in-one docker compose and basic Scala Main class that can be used to test your Kafka Streams application locally.

Start kafka 
```bash
docker compose up
```

Once it is running view the confluent control center at http://localhost:9021/

You will want to create a topic called `test-topic` with 100 partitions or whatever you choose to test.

Run the scala app in a new terminal.
```bash
sbt run
```
