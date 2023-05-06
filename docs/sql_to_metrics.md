# Extract metrics from SQL List

### Usage

- For **Command Line Interface**: please refer to [Usage of Command Line Interface](cli.md)
- For **Server mode and REST API**: please refer to [Usage of Server Mode](server.md)

## Example

### Extract Metrics from SQLs to ZenML file

```
# please replace ${Kyligence-ZenML-Toolkit-{version}} with absolute path
$ cd ${Kyligence-ZenML-Toolkit-{version}}
$ sh ./bin/zen.sh -i ${Kyligence-ZenML-Toolkit-{version}}/samples/sql/ssb.sql -o ${Kyligence-ZenML-Toolkit-{version}}/samples/sql
```

You will get a `ssb.zen.yml` file in folder `Kyligence-ZenML-Toolkit-{version}/samples/sql`

### Import ZenML to Kyligence Zen

1. Login into [Kyligence Zen](https://kyligence.io/zen)
2. Go to **Data**, click **New**, Choose **Table**
   ![New Table](images/examples/import_table.png)
3. Choose upload csv, upload `ssb.*.csv` files in folder `Kyligence-ZenML-Toolkit-{version}/samples/ssb`
   ![Upload Table](images/examples/import_table2.png)
4. Create a view `lineorder_l_part_l_supplier_l_customer_l_dates` as ssb data model
   
5. Go to **Metrics**, click **Import**
   
6. Import `ssb.zen.yml` file

7. All metrics defined in sql file will be imported to Kyligence Zen
  

## Implementation


## Notice