SELECT
	p.id AS pmt_id
	, ip.payment_date AS pmt_date
	, tx.amount AS pmt_amount
	, inv.record_id AS pmt_invoice_id
	, ip.id AS pmt_invoice_pmt_id
FROM invoice_payments ip
INNER JOIN payments p ON p.id = ip.payment_id
INNER JOIN payment_transactions tx ON tx.payment_id = ip.payment_id
INNER JOIN invoices inv ON inv.id = ip.invoice_id
WHERE p.account_record_id = ?
ORDER BY ip.payment_date, inv.record_id