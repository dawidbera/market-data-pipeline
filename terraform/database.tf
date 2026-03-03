resource "postgresql_extension" "timescaledb" {
  name = "timescaledb"
}

# Tables
resource "postgresql_table" "ticks" {
  name     = "ticks"
  schema   = "public"
  database = var.db_name

  column {
    name = "id"
    type = "bigserial"
  }
  column {
    name = "symbol"
    type = "varchar(10)"
    nullable = false
  }
  column {
    name = "price"
    type = "double precision"
    nullable = false
  }
  column {
    name = "volume"
    type = "bigint"
    nullable = false
  }
  column {
    name = "timestamp"
    type = "timestamp with time zone"
    nullable = false
  }

  primary_key {
    columns = ["id", "timestamp"]
  }

  depends_on = [postgresql_extension.timescaledb]
}

resource "postgresql_table" "candles" {
  name     = "candles"
  schema   = "public"
  database = var.db_name

  column {
    name = "id"
    type = "bigserial"
  }
  column {
    name = "symbol"
    type = "varchar(10)"
    nullable = false
  }
  column {
    name = "open"
    type = "double precision"
    nullable = false
  }
  column {
    name = "high"
    type = "double precision"
    nullable = false
  }
  column {
    name = "low"
    type = "double precision"
    nullable = false
  }
  column {
    name = "close"
    type = "double precision"
    nullable = false
  }
  column {
    name = "volume"
    type = "bigint"
    nullable = false
  }
  column {
    name = "window_start"
    type = "timestamp with time zone"
    nullable = false
  }
  column {
    name = "window_end"
    type = "timestamp with time zone"
    nullable = false
  }

  primary_key {
    columns = ["id", "window_start"]
  }

  depends_on = [postgresql_extension.timescaledb]
}

resource "postgresql_table" "alerts" {
  name     = "alerts"
  schema   = "public"
  database = var.db_name

  column {
    name = "id"
    type = "bigserial"
  }
  column {
    name = "symbol"
    type = "varchar(10)"
    nullable = false
  }
  column {
    name = "type"
    type = "varchar(50)"
    nullable = false
  }
  column {
    name = "message"
    type = "text"
    nullable = false
  }
  column {
    name = "timestamp"
    type = "timestamp with time zone"
    nullable = false
  }

  primary_key {
    columns = ["id", "timestamp"]
  }

  depends_on = [postgresql_extension.timescaledb]
}

# Convert to Hypertables
# Note: This requires psql to be installed on the machine running terraform.
resource "null_resource" "convert_to_hypertables" {
  depends_on = [
    postgresql_table.ticks,
    postgresql_table.candles,
    postgresql_table.alerts
  ]

  provisioner "local-exec" {
    command = <<EOF
      PGPASSWORD=${var.db_password} psql -h ${var.db_host} -p ${var.db_port} -U ${var.db_username} -d ${var.db_name} -c "SELECT create_hypertable('ticks', 'timestamp', if_not_exists => TRUE);"
      PGPASSWORD=${var.db_password} psql -h ${var.db_host} -p ${var.db_port} -U ${var.db_username} -d ${var.db_name} -c "SELECT create_hypertable('candles', 'window_start', if_not_exists => TRUE);"
      PGPASSWORD=${var.db_password} psql -h ${var.db_host} -p ${var.db_port} -U ${var.db_username} -d ${var.db_name} -c "SELECT create_hypertable('alerts', 'timestamp', if_not_exists => TRUE);"
EOF
  }
}
