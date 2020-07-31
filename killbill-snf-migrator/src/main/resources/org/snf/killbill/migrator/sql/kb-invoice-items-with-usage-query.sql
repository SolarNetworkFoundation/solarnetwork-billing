SELECT 
	inv.record_id AS inv_record_id
	, inv.target_date AS inv_date
	, CASE itm.type
		WHEN 'TAX' THEN CONCAT(inv.record_id, itm.description)
		ELSE itm.id
		END AS itm_id
	, CASE itm.type
		WHEN 'CBA_ADJ' THEN 'Credit'
		WHEN 'TAX' THEN 'Tax'
		WHEN 'USAGE' THEN 'Usage'
		ELSE CONCAT('?', itm.type)
		END AS itm_type
	, CASE itm.description
		WHEN 'posted-datum-metric-daily-usage' THEN 'datum-props-in'
		WHEN 'queried-datum-daily-usage' THEN 'datum-out'
		WHEN 'stored-datum-daily-usage' THEN 'datum-days-stored'
		WHEN 'Adjustment (use of account credit)' THEN 'account-credit'
		WHEN 'Adjustment (account credit)' THEN 'account-credit-add'
		ELSE itm.description
		END AS itm_key
	, CASE itm.type
		WHEN 'TAX' THEN NULL
		ELSE itm.amount
		END AS itm_amount
	, SUM(CASE itm.type
		WHEN 'TAX' THEN itm.amount
		ELSE NULL
		END) AS itm_amount_sum
	, itm.start_date AS itm_start_date
	, itm.end_date AS itm_end_date
	, CASE itm.type
		WHEN 'TAX' THEN NULL
		ELSE f.field_value
		END AS itm_node_id
	, SUM(useage.amount) AS usage_amount
FROM invoices inv
INNER JOIN invoice_items itm ON itm.invoice_id = inv.id
LEFT OUTER JOIN custom_fields f ON f.object_id = itm.subscription_id AND f.field_name = 'nodeId'
LEFT OUTER JOIN rolled_up_usage useage ON itm.type = 'USAGE' 
	AND useage.subscription_id = itm.subscription_id
	AND useage.record_date >= itm.start_date
	AND useage.record_date < itm.end_date
WHERE inv.account_record_id = ?
GROUP BY inv_record_id, inv_date, itm_id, itm_type, itm_key
ORDER BY inv.target_date
	, inv.record_id
	, CASE itm.type WHEN 'FIXED' THEN 1 WHEN 'USAGE' THEN 2 WHEN 'TAX' THEN 3 WHEN 'CBA_ADJ' THEN 4 ELSE 0 END
	, f.field_value
	, itm.description
	, useage.record_date
