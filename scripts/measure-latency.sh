#!/bin/bash

# Configuration
INGESTOR_URL="http://localhost:8082/api/ingest/tick"
DASHBOARD_URL="http://localhost:8080/api/market/alerts"
SYMBOL="LATENCY_TEST_$(date +%s)"
VOLUME=5000 # High volume to trigger an alert immediately

echo "--- Starting End-to-End Latency Test ---"
echo "Symbol: $SYMBOL"
echo "Threshold Volume: $VOLUME (triggers HIGH_VOLUME alert)"

# 1. Start time
START_TIME=$(date +%s%3N)

# 2. Send high volume tick to Ingestor
curl -s -X POST "$INGESTOR_URL" \
     -H "Content-Type: application/json" \
     -d "{\"symbol\": \"$SYMBOL\", \"price\": 100.0, \"volume\": $VOLUME}" > /dev/null

echo "Tick sent at: $START_TIME ms"

# 3. Poll Dashboard API for the alert
echo "Polling Dashboard for alert..."
MAX_RETRIES=100
RETRY_COUNT=0
SUCCESS=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    # Check alerts for our unique symbol
    RESPONSE=$(curl -s "$DASHBOARD_URL/$SYMBOL")
    
    if [[ "$RESPONSE" == *"$SYMBOL"* ]]; then
        END_TIME=$(date +%s%3N)
        LATENCY=$((END_TIME - START_TIME))
        echo "Alert received at: $END_TIME ms"
        echo "----------------------------------------"
        echo "SUCCESS: Alert detected in Dashboard DB!"
        echo "End-to-End Latency: $LATENCY ms"
        echo "----------------------------------------"
        SUCCESS=true
        break
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
    sleep 0.1
done

if [ "$SUCCESS" = false ]; then
    echo "FAILED: Alert not found in Dashboard after $((MAX_RETRIES * 100)) ms"
    exit 1
fi
