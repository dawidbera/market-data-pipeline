resource "postgresql_extension" "timescaledb" {
  name = "timescaledb"
}

# Tables & Hypertables Initialization
# Note: A null_resource is used because the standard postgresql provider does not manage tables efficiently
# and TimescaleDB commands are easier to run via raw SQL.
# Docker exec is used to run psql inside the container since psql might not be on the host.
resource "null_resource" "db_init" {
  depends_on = [postgresql_extension.timescaledb]

  triggers = {
    schema_hash = filebase64sha256("${path.module}/../dashboard-backend/src/main/resources/schema.sql")
  }

  provisioner "local-exec" {
    command = <<EOF
      docker exec -i market-db psql -U ${var.db_username} -d ${var.db_name} < ${path.module}/../dashboard-backend/src/main/resources/schema.sql
EOF
  }
}
