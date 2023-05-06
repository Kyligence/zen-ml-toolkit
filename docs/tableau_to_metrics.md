# Extract Tableau Calculated Field to Metrics

### Usage

- For **Command Line Interface**: please refer to [Usage of Command Line Interface](cli.md)
- For **Server mode and REST API**: please refer to [Usage of Server Mode](server.md)

## Example

### Convert tableau tds file to ZenML file

```
# please replace ${Kyligence-ZenML-Toolkit-{version}} with absolute path
$ cd ${Kyligence-ZenML-Toolkit-{version}}
$ sh ./bin/zen.sh -i ${Kyligence-ZenML-Toolkit-{version}}/samples/tableau/superstore.tds -o ${Kyligence-ZenML-Toolkit-{version}}/samples/tableau
```

You will get a `superstore.zen.yml` file in folder `Kyligence-ZenML-Toolkit-{version}/samples/tableau`

### Import ZenML to Kyligence Zen

1. Login into [Kyligence Zen](https://kyligence.io/zen)
2. Go to **Data**, click **New**, Choose **Table**
   ![New Table](images/examples/import_table.png)
3. Choose upload csv, upload `orders.csv` file in folder `Kyligence-ZenML-Toolkit-{version}/samples`
   ![Upload Table](images/examples/import_table2.png)
4. You will create an `orders` table after you finished all steps in wizard
   ![Table](images/examples/import_table3.png)
5. Go to **Metrics**, click **Import**
   ![New Metrics](images/examples/import_zen.png)
6. Import `superstore.zen.yml` file
   ![Import Zen Yaml](images/examples/import_zen2.png)
7. All metrics defined in tableau tds file will be imported to Kyligence Zen
   ![Metrics in Zen](images/examples/metrics_in_zen.png)

## Implementation


## Notice