resource "kafka_topic" "raw_ticks" {
  name               = "market.data.raw"
  replication_factor = 1
  partitions         = 3

  config = {
    "cleanup.policy" = "delete"
    "retention.ms"   = "3600000" # 1 hour for raw data
  }
}

resource "kafka_topic" "aggregated_candles" {
  name               = "market.data.aggregated"
  replication_factor = 1
  partitions         = 1

  config = {
    "cleanup.policy" = "compact"
  }
}

resource "kafka_topic" "alerts" {
  name               = "market.data.alerts"
  replication_factor = 1
  partitions         = 1

  config = {
    "cleanup.policy" = "delete"
    "retention.ms"   = "86400000" # 24 hours for alerts
  }
}
