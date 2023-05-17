import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{ Producer, ProducerSettings }
import zio.kafka.serde._
import zio.stream.ZStream

object MainApp extends ZIOAppDefault {
  val producer =
    ZStream
      .repeatZIO(Random.nextIntBetween(0, Int.MaxValue))
      .mapZIO { random =>
        Producer.produce[Any, Long, String](
          topic = "test_100",
          key = random % 1000,
          value = random.toString,
          keySerializer = Serde.long,
          valueSerializer = Serde.string
        )
      }
      .take(10000)
      .drain

  val consumer =
    Consumer
      .partitionedStream(Subscription.topics("test_100"), Serde.long, Serde.string)
      .flatMapPar(Int.MaxValue) { case (_, partitionStream) =>
        partitionStream
          .map(_.offset)
          .tap(x => ZIO.logInfo(s"Offset: ${x.offset}"))

      }
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)

  def producerLayer =
    ZLayer.scoped(
      Producer.make(
        settings = ProducerSettings(List("localhost:9092"))
      )
    )

  def consumerLayer =
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("localhost:9092")).withGroupId("group")
      )
    )

  override def run = {
    for {
      p <- producer.runDrain // Produce all the test messages.
      c <- consumer.runDrain // Then process them after to drop the queue.
    } yield ()

  }.provide(producerLayer, consumerLayer)
}
