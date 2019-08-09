# time-to-pay-taxpayer

The Taxpayer service is used in the SSTTP project for Pay What You Owe In Instalments. It has one endpoint, a GET which will retrieve Taxpayer
information for a given UTR. Below is a diagram showing where the Taxpayer service fits into the SSTTP project.

<a href="https://github.com/hmrc/time-to-pay-taxpayer">
    <p align="center">
        <img src="https://raw.githubusercontent.com/hmrc/time-to-pay-taxpayer/master/public/taxpayer.png" alt="TaxpayerOverview">
    </p>
</a>

## Run locally

This app depends on 3 DES services and the SA app. Both have stub projects available:

https://github.com/hmrc/self-service-time-to-pay-des-stub-scala

https://github.com/hmrc/sa-stub

To start the app either clone this repository and the 2 stub ones and run **sbt run** or start as Play apps.

Alternatively you can use Service Manager - **sm --start TIME_TO_PAY_TAX_PAYER_DEP -f**

The app will start by default on port 9857. Stub data should be configured for the UTR 1234567890 so you should be able to GET from http://localhost:9857/tax-payer/1234567890

## GET /taxpayer/{utr}

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

