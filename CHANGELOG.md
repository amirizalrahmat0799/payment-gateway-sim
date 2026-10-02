# Changelog

All notable changes to this project. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and versions follow [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Dependabot: weekly, grouped minor and patch updates for Maven, the Docker base images and GitHub Actions, so CI checks them together. Major upgrades are left
  for deliberate, hand-made changes.

## [1.0.0] - 2026-09-30

### Added
- Four Spring Boot microservices: merchant onboarding with API keys, card tokenization (AES-GCM vault with HMAC
  fingerprints), payments (authorize, capture, void, refund) and settlement.
- Idempotency keys on payment creation, a transactional outbox relayed to Kafka with `FOR UPDATE SKIP LOCKED`, an
  idempotent ledger consumer and a dead-letter topic.
- Daily settlement as a Spring Batch job that can't settle the same date twice.
- `GET /merchants/me` for the merchant dashboard.
- Prometheus metrics, Kubernetes liveness/readiness probes and graceful shutdown.
- Docker Compose setup, Swagger UI, `.http` demo requests and a GitHub Actions build.

[Unreleased]: https://github.com/amirizalrahmat0799/payment-gateway-sim/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/amirizalrahmat0799/payment-gateway-sim/releases/tag/v1.0.0
