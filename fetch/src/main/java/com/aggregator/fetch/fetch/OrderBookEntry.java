package com.aggregator.fetch.fetch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderBookEntry(@JsonProperty("px") double px,
                             @JsonProperty("qty") double qty) {
}
