# time-to-pay-taxpayer

#### GET /tax-payer/{utr}

Retrieves a definition of a tax payer

Output
```
{
   "customerName": "Customer name",
   "addresses": [
           {
             "addressLine1": "123 Fake Street",
             "addressLine2": "Foo",
             "addressLine3": "Bar",
             "addressLine4": "",
             "addressLine5": "",
             "postcode": "BN3 2GH"
           }
         ],
    "selfAssessment": {
      "utr": "1234567890",
      "communicationPreferences": {
        "welshLanguageIndicator": true,
        "audioIndicator": true,
        "largePrintIndicator": true,
        "brailleIndicator": true
      },
      "debits": [
        {
          "originCode": "POA2",
          "amount": 250.52,
          "dueDate": "2016-01-31",
          "interest": {
            "calculationDate" : "2016-06-01",
            "amountAccrued" : 42.32
          },
          "taxYearEnd": "2017-04-05"
        }
      ],
      "returns":[
        {
          "taxYearEnd":"2014-04-05",
          "receivedDate":"2014-11-28"
        },
        {
          "taxYearEnd":"2014-04-05",
          "issuedDate":"2015-04-06",
          "dueDate":"2016-01-31"
        },
        {
          "taxYearEnd":"2014-04-05",
          "issuedDate":"2016-04-06",
          "dueDate":"2017-01-31",
          "receivedDate":"2016-04-11"
        }
      ]
   }
}
```

| Status Code | Description |
|---|---|
| 200 | Returns tax payer                           |
| 404 | No tax payer found for the UTR specified    |

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
