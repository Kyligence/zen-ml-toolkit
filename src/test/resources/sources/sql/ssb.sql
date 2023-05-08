select sum(lo_revenue) as revenue
from lineorder
         left join dates on lineorder.lo_orderdate = dates.d_datekey
where d_datekey = '1993-01-01'
  and lo_discount between 1 and 3
  and lo_quantity < 25;

select sum(lo_revenue) as revenue
from lineorder
         left join dates on lo_orderdate = d_datekey
where d_yearmonthnum = 199401
  and lo_discount between 4 and 6
  and lo_quantity between 26 and 35;

select sum(lo_revenue) as revenue
from lineorder
         left join dates on lo_orderdate = d_datekey
where d_weeknuminyear = 6 and d_datekey > '1994-01-01' and d_datekey < '1995-01-01'
  and lo_discount between 5 and 7
  and lo_quantity between 26 and 35;

select sum(lo_revenue) as lo_revenue, d_year, p_brand
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join part on lo_partkey = p_partkey
         left join supplier on lo_suppkey = s_suppkey
where p_category = 'MFGR#12' and s_region = 'AMERICA'
group by d_year, p_brand
order by d_year, p_brand;


select sum(lo_revenue) as lo_revenue, d_year, p_brand
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join part on lo_partkey = p_partkey
         left join supplier on lo_suppkey = s_suppkey
where p_brand between 'MFGR#2221' and 'MFGR#2228' and s_region = 'ASIA'
group by d_year, p_brand
order by d_year, p_brand;

select sum(lo_revenue) as lo_revenue, d_year, p_brand
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join part on lo_partkey = p_partkey
         left join supplier on lo_suppkey = s_suppkey
where p_brand = 'MFGR#2239' and s_region = 'EUROPE'
group by d_year, p_brand
order by d_year, p_brand;

select c_nation, s_nation, d_year, sum(lo_revenue) as lo_revenue
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join customer on lo_custkey = c_custkey
         left join supplier on lo_suppkey = s_suppkey
where c_region = 'ASIA' and s_region = 'ASIA'and d_datekey >= '1992-01-01' and d_datekey <= '1997-01-01'
group by c_nation, s_nation, d_year
order by d_year asc, lo_revenue desc;

select c_city, s_city, d_year, sum(lo_revenue) as lo_revenue
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join customer on lo_custkey = c_custkey
         left join supplier on lo_suppkey = s_suppkey
where c_nation = 'UNITED STATES' and s_nation = 'UNITED STATES'
  and d_datekey >= '1992-01-01' and d_datekey <= '1997-01-01'
group by c_city, s_city, d_year
order by d_year asc, lo_revenue desc;

select c_city, s_city, d_year, sum(lo_revenue) as lo_revenue
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join customer on lo_custkey = c_custkey
         left join supplier on lo_suppkey = s_suppkey
where (c_city='UNITED KI1' or c_city='UNITED KI5')
  and (s_city='UNITED KI1' or s_city='UNITED KI5')
  and d_datekey >= '1992-01-01' and d_datekey <= '1997-01-01'
group by c_city, s_city, d_year
order by d_year asc, lo_revenue desc;

select c_city, s_city, d_year, sum(lo_revenue) as lo_revenue
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join customer on lo_custkey = c_custkey
         left join supplier on lo_suppkey = s_suppkey
where (c_city='UNITED KI1' or c_city='UNITED KI5') and (s_city='UNITED KI1' or s_city='UNITED KI5') and d_yearmonth = 'Dec1997'
group by c_city, s_city, d_year
order by d_year asc, lo_revenue desc;

select d_year, c_nation, sum(lo_revenue) - sum(lo_supplycost) as profit
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join customer on lo_custkey = c_custkey
         left join supplier on lo_suppkey = s_suppkey
         left join part on lo_partkey = p_partkey
where c_region = 'AMERICA' and s_region = 'AMERICA' and (p_mfgr = 'MFGR#1' or p_mfgr = 'MFGR#2')
group by d_year, c_nation
order by d_year, c_nation;

select d_year, s_nation, p_category, sum(lo_revenue) - sum(lo_supplycost) as profit
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join customer on lo_custkey = c_custkey
         left join supplier on lo_suppkey = s_suppkey
         left join part on lo_partkey = p_partkey
where c_region = 'AMERICA'and s_region = 'AMERICA'
  and d_datekey >= '1997-01-01' and d_datekey < '1999-01-01'
  and (p_mfgr = 'MFGR#1' or p_mfgr = 'MFGR#2')
group by d_year, s_nation, p_category
order by d_year, s_nation, p_category;

select d_year, s_city, p_brand, sum(lo_revenue) - sum(lo_supplycost) as profit
from lineorder
         left join dates on lo_orderdate = d_datekey
         left join customer on lo_custkey = c_custkey
         left join supplier on lo_suppkey = s_suppkey
         left join part on lo_partkey = p_partkey
where c_region = 'AMERICA'and s_nation = 'UNITED STATES'
  and d_datekey >= '1997-01-01' and d_datekey < '1999-01-01'
  and p_category = 'MFGR#14'
group by d_year, s_city, p_brand
order by d_year, s_city, p_brand;
