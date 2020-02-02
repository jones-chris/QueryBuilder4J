-- Create county_spending_detail table
CREATE TABLE public.county_spending_detail
(
  fiscal_year_period smallint,
  fiscal_year smallint,
  service character varying(200),
  department character varying(200),
  program character varying(200),
  amount numeric(16,2)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE public.county_spending_detail
  OWNER TO postgres;

INSERT INTO county_spending_detail
(
  fiscal_year_period,
  fiscal_year,
  service,
  department,
  program,
  amount
)
VALUES
(
  4,
  2017,
  'General Government',
  'Liquor Control',
  'Retail Sales Operations',
  214.85
);

INSERT INTO county_spending_detail
(
  fiscal_year_period,
  fiscal_year,
  service,
  department,
  program,
  amount
)
VALUES
(
  6,
  2014,
  'General Government',
  'Human Resources',
  'Health & Employee Welfare',
  1576281.42
);

INSERT INTO county_spending_detail
(
  fiscal_year_period,
  fiscal_year,
  service,
  department,
  program,
  amount
)
VALUES
(
  5,
  2017,
  'Housing and Community Development',
  'Housing and Community Affairs',
  'Multi-Family Housing Programs',
  200
);

INSERT INTO county_spending_detail
(
  fiscal_year_period,
  fiscal_year,
  service,
  department,
  program,
  amount
)
VALUES
(
  5,
  2017,
  'Transportation',
  'Transportation',
  'Administration',
  878
);

INSERT INTO county_spending_detail
(
  fiscal_year_period,
  fiscal_year,
  service,
  department,
  program,
  amount
)
VALUES
(
  5,
  2017,
  'General Government',
  'Liquor Control',
  'Warehouse Operations',
  198459.06
);

INSERT INTO county_spending_detail
(
  fiscal_year_period,
  fiscal_year,
  service,
  department,
  program,
  amount
)
VALUES
(
  6,
  2017,
  'Health and Human Services',
  'Health and Human Services',
  'Outpatient Behavioral Health Services - Adult',
  169.02
);

-- Create periods table
CREATE TABLE public.periods
(
  period smallint,
  quarter character(2)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE public.periods
  OWNER TO postgres;

INSERT INTO public.periods(
            period, quarter)
    VALUES (1, 'Q1');


INSERT INTO public.periods(
            period, quarter)
    VALUES (2, 'Q1');


INSERT INTO public.periods(
            period, quarter)
    VALUES (3, 'Q1');


INSERT INTO public.periods(
            period, quarter)
    VALUES (4, 'Q2');

INSERT INTO public.periods(
            period, quarter)
    VALUES (5, 'Q2');

INSERT INTO public.periods(
            period, quarter)
    VALUES (6, 'Q2');

INSERT INTO public.periods(
            period, quarter)
    VALUES (7, 'Q3');

INSERT INTO public.periods(
            period, quarter)
    VALUES (8, 'Q3');

INSERT INTO public.periods(
            period, quarter)
    VALUES (9, 'Q3');

INSERT INTO public.periods(
            period, quarter)
    VALUES (10, 'Q4');

INSERT INTO public.periods(
            period, quarter)
    VALUES (11, 'Q4');

INSERT INTO public.periods(
            period, quarter)
    VALUES (12, 'Q4');

-- Create service_hierarchy table
CREATE TABLE public.service_hierarchy
(
  fiscal_year smallint,
  service character varying(50),
  service_owner character varying(10)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE public.service_hierarchy
  OWNER TO postgres;

INSERT INTO public.service_hierarchy(
            fiscal_year, service, service_owner)
    VALUES (2017, 'General Government', 'Jake');

INSERT INTO public.service_hierarchy(
            fiscal_year, service, service_owner)
    VALUES (2014, 'General Government', 'Sam');

INSERT INTO public.service_hierarchy(
            fiscal_year, service, service_owner)
    VALUES (2017, 'Housing and Community Development', 'Bob');

INSERT INTO public.service_hierarchy(
            fiscal_year, service, service_owner)
    VALUES (2017, 'Transportation', 'Sue');

INSERT INTO public.service_hierarchy(
            fiscal_year, service, service_owner)
    VALUES (2017, 'Health and Human Services', 'Sally');
