#!/bin/bash

LOG_FILE="test_run.log"
SUCCESS_COUNT_FILE="last_success_count.txt"

# Ensure script is run from its own directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

log() {
    echo "$(date): $1" >> "$LOG_FILE"
    echo "$1"
}

find_test_methods() {
    # Include both *Test.java and *Tests.java files
    local test_files=$(find .. \( -name "*Test.java" -o -name "*Tests.java" \) -type f)
    local entries=()

    for file in $test_files; do
        # Extract module name from path (e.g., ../dashboard-backend/src/...)
        local module=$(echo "$file" | cut -d'/' -f2)
        local package=$(grep "^package " "$file" | sed 's/package \([a-zA-Z0-9_.]*\);/\1/')
        local class_name=$(basename "$file" .java)
        local full_class="$package.$class_name"
        # Improved awk logic to handle intermediate annotations like @DisplayName
        local test_methods=$(awk '/@Test/{flag=1; next} flag && /^[[:space:]]*@/ {next} flag && /void [a-zA-Z_][a-zA-Z0-9_]*\(/ {print gensub(/.*void ([^(]*)\(.*/, "\\1", "g"); flag=0}' "$file")
        
        for method in $test_methods; do
            if [ -n "$method" ]; then
                # Store as "module:full_class.method"
                entries+=("$module:$full_class.$method")
            fi
        done
    done

    echo "${entries[@]}"
}

main() {
    log "Starting sequential test execution method by method"

    # Read previous stats before overwriting the file
    local prev_total=0
    local prev_success=0
    if [ -f "$SUCCESS_COUNT_FILE" ]; then
        read -r prev_total prev_success < "$SUCCESS_COUNT_FILE"
    fi

    local test_entries=($(find_test_methods))
    local total_methods=${#test_entries[@]}
    local success_count=0

    log "Found $total_methods test methods"

    for entry in "${test_entries[@]}"; do
        local module=$(echo "$entry" | cut -d':' -f1)
        local method=$(echo "$entry" | cut -d':' -f2)
        
        log "Running test: $method in module :$module"
        # Execute specifically for the target module using -p .. to set project root
        if ../gradlew -p .. ":$module:test" --tests "$method" --console=plain >> "$LOG_FILE" 2>&1; then
            ((success_count++))
            log "Test $method passed. Successes: $success_count/$total_methods"
        else
            log "Test $method failed. Stopping."
            break
        fi
    done

    # Save current results for future comparisons
    echo "$total_methods $success_count" > "$SUCCESS_COUNT_FILE"

    if [ "$prev_total" -gt 0 ]; then
        local prev_percent=$((prev_success * 100 / prev_total))
        local curr_percent=$((success_count * 100 / total_methods))
        if [ "$curr_percent" -gt "$prev_percent" ]; then
            log "More methods succeeded: $curr_percent% vs $prev_percent%"
        elif [ "$curr_percent" -lt "$prev_percent" ]; then
            log "Fewer methods succeeded: $curr_percent% vs $prev_percent%"
        else
            log "Same success percentage: $curr_percent%"
        fi
    else
        log "No previous data to compare"
    fi

    log "Finished. Successes: $success_count/$total_methods"
}

main "$@"
