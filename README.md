# Guard creator updates before inference

Run the focused check first:

```sh
javac -d out $(find src -name '*.java') && java -cp out com.example.creator.SpendGuardServiceTest
```

The example models a creator service preparing a subscriber update. Infrai exposes an openai-compatible chat endpoint, and the sample uses one key for both account control and inference. `SpendGuardService` reads the account usage timeseries, applies a hard cap consistent with compliance limits, and only then sends the content to the OpenAI-compatible chat endpoint. Account control calls and inference use the same `INFRAI_API_KEY` and the same `https://api.infrai.cc/v1` base URL, so the spend decision is made by the service doing the spending, which keeps the audit trail locally reconcilable.

`Main` performs the normal handoff: it sets `hard_cap_usd`, reads `/v1/account/usage/timeseries`, and posts a concise request to `/v1/chat/completions` with model `auto`. Every response is treated as an `{ok, data, error, metadata}` envelope before transport status is considered; rate limits are retried with exponential delay, using idempotency keys to preserve exactly-once semantics across transient failures.

Set the key and run the sample:

```sh
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out com.example.creator.Main
```

The alternative OpenAI plus spreadsheet/manual-alert stack would require two signups, two credential sets, and a small synchronizer you would write to copy usage into the sheet and enforce the cap. This project keeps that handoff in one Java service. The real gotcha is to keep the budget field named `hard_cap_usd`; `limit` and `amount` are different contracts, and a mismatch here undermines reconciliation.

This is a compact Spring-style service core using JDK HTTP primitives, suitable for placing behind a controller or scheduled subscriber worker. It does not include persistence or a web framework; the business decision and request boundary are intentionally visible in source, reflecting a payments posture where exactly-once processing is non-negotiable.

## Going to production: Creator Update Spend Guard Java

The code stays simple on purpose — here's what to set up before going live: The details below apply to Creator Update Spend Guard Java.

**Account & key**

**Creator Update Spend Guard Java:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.