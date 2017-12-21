
# Northwestern mutual CSV import

### Two steps import: generate and import document with blobs

1. Run a random producers of document messages, these message represent Folder and File document a blob. The total number of document created is: `nbThreads * nbDocuments`.
  ```
curl -X POST 'http://localhost:8080/nuxeo/site/automation/ACA.runCSVDocumentProducers' -u Administrator:Administrator -H 'content-type: application/json+nxrequest' \
  -d '{"params":{"csvPath": "/path/to/csv/test.csv", "nbThreads": 1}}'
```

| Params| Default | Description |
| --- | ---: | --- |
| `csvPath` | `` | The path to the csv file to import |
| `nbThreads` | `8` | The number of concurrent producer to run => concurrent import is not implemented so more than one thread will only recreate duplicate documents. But it may be needed to implement to make ingest faster |
| `mqName` | `mq-doc` | The name of the MQueue. |
| `mqSize` | `$nbThreads` |The size of the MQueue which will fix the maximum number of consumer threads |
| `mqBlobInfo` |  | A MQueue containing blob information to use, see section below for use case |
| `mqConfig` | `import` | The MQ configuration name |

2. Run consumers of document messages creating Nuxeo documents, the concurrency will match the previous nbThreads producers parameters
  ```
curl -X POST 'http://localhost:8080/nuxeo/site/automation/ACA.runDocumentConsumers' -u Administrator:Administrator -H 'content-type: application/json+nxrequest' \
  -d '{"params":{"rootFolder": "/default-domain/worskpaces"}}'
```

| Params| Default | Description |
| --- | ---: | --- |
| `rootFolder` |  | The path of the Nuxeo container to import documents, this document must exists |
| `repositoryName` |  | The repository name used to import documents |
| `nbThreads` | `mqSize` | The number of concurrent consumer, should not be greater than the mqSize |
| `batchSize` | `10` | The consumer commit documents every batch size |
| `batchThresholdS` | `20` | The consumer commit documents if the transaction is longer that this threshold |
| `retryMax` | `3` | Number of time a consumer retry to import in case of failure |
| `retryDelayS` | `2` | Delay between retries |
| `mqName` | `mq-doc` | The name of the MQueue to tail |
| `mqConfig` | `import` | The MQ configuration name |
| `useBulkMode` | `false` | Process asynchronous listeners in bulk mode |
| `blockIndexing` | `false` | Do not index created document with Elasticsearch |
| `blockAsyncListeners` | `false` | Do not process any asynchronous listeners |
| `blockPostCommitListeners` | `false` | Do not process any post commit listeners |
| `blockDefaultSyncListeners` | `false` | Disable some default synchronous listeners: dublincore, mimetype, notification, template, binarymetadata and uid |
