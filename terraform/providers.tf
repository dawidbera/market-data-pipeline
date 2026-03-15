terraform {
  required_providers {
    kafka = {
      source  = "Mongey/kafka"
      version = "~> 0.13.0"
    }
    postgresql = {
      source  = "cyrilgdn/postgresql"
      version = "~> 1.25.0"
    }
  }
}

provider "kafka" {
  bootstrap_servers = [var.kafka_bootstrap_servers]
  tls_enabled       = false
}

provider "postgresql" {
  host            = var.db_host
  port            = var.db_port
  database        = var.db_name
  username        = var.db_username
  password        = var.db_password
  sslmode         = "disable"
  connect_timeout = 15
}
