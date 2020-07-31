SELECT 
	inv.record_id AS inv_record_id
	, inv.target_date AS inv_date
	, itm.id AS itm_id
	, CASE itm.type
		WHEN 'CBA_ADJ' THEN 'account-credit'
		WHEN 'TAX' THEN 'Tax'
		WHEN 'USAGE' THEN 'Usage'
		ELSE CONCAT('?', itm.type)
		END AS itm_type
	, CASE itm.description
		WHEN 'posted-datum-metric-daily-usage' THEN 'datum-props-in'
		WHEN 'queried-datum-daily-usage' THEN 'datum-out'
		WHEN 'stored-datum-daily-usage' THEN 'datum-days-stored'
		ELSE itm.description
		END AS itm_key
	, itm.amount
	, itm.start_date AS itm_start_date
	, itm.end_date AS itm_end_date
	, f.field_value AS itm_node_id
	, useage.amount AS usage_amount
FROM invoices inv
INNER JOIN invoice_items itm ON itm.invoice_id = inv.id
LEFT OUTER JOIN custom_fields f ON f.object_id = itm.subscription_id AND f.field_name = 'nodeId'
LEFT OUTER JOIN rolled_up_usage useage ON itm.type = 'USAGE' 
	AND useage.subscription_id = itm.subscription_id
	AND useage.record_date >= itm.start_date
	AND useage.record_date < itm.end_date
WHERE inv.account_record_id = ?
ORDER BY inv.target_date
	, inv.record_id
	, CASE itm.type WHEN 'FIXED' THEN 1 WHEN 'USAGE' THEN 2 WHEN 'TAX' THEN 3 ELSE 0 END
	, f.field_value
	, itm.description
	, useage.record_date
