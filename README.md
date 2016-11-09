# time-to-pay-eligibility

[![Build Status](https://travis-ci.org/hmrc/time-to-pay-eligibility.svg)](https://travis-ci.org/hmrc/time-to-pay-eligibility) [ ![Download](https://api.bintray.com/packages/hmrc/releases/time-to-pay-eligibility/images/download.svg) ](https://bintray.com/hmrc/releases/time-to-pay-eligibility/_latestVersion)

#### GET /time-to-pay-eligibility/tax-payer/{utr}

Retrieves a definition of a tax payer

Input
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
             "postCode": "BN3 2GH"
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
          "originCode": "IN2",
          "dueDate": "2014-07-31"
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