package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
)

func main() {
	serviceVersion := os.Getenv("SERVICE_VERSION")
	if serviceVersion == "" {
	    serviceVersion = "v1"
	}

	enableFeatureX := os.Getenv("ENABLE_FEATURE_X") == "true"

	http.HandleFunc("/ping", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "pong from service %s", serviceVersion)
	})

	if enableFeatureX {
		http.HandleFunc("/feature", func(w http.ResponseWriter, r *http.Request) {
			fmt.Fprintf(w, "Feature X is enabled!")
		})
	}

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
