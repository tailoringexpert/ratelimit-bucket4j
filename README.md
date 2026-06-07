# ailoringexpert-reatelimiting-bucket4j

Simple rate limiting module to be used by TailoringeXpert.
It is intendeded for use in non distributed environments.

## Configuration

Configuration is inspired by [bucket4j-spring-boot-starter](https://github.com/MarcGiffing/bucket4j-spring-boot-starter), but only a few properties are taken into account.

```yaml
bucket4j:
  filters:
    rate-limits:
      - bandwidths:
          - capacity: 20
            time: 1
            unit: minutes
            refill-speed: greedy
          - capacity: 10
            time: 10
            unit: seconds
            refill-speed: interval
```
