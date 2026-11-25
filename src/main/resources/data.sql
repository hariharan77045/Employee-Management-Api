-- Mock data for practice, dated relative to the day the app first starts so the
-- dashboard widgets ("Who's off", attendance, payslips) have something current to show.
--
-- Runs after schema.sql on every start, but each insert only happens while the
-- 'current-activity-v1' marker is missing. To insert a fresh set later, delete the
-- rows you don't want and run: DELETE FROM mock_data_seed WHERE name = 'current-activity-v1';

-- 1. Leave requests: some approved around today, some pending for the approvals queue.
INSERT INTO leaverequest (leaveid, employeeid, leavetype, fromdate, todate, reason, status)
SELECT nextval('leaverequest_seq'), seed.employeeid, seed.leavetype,
       CURRENT_DATE + seed.fromoffset, CURRENT_DATE + seed.tooffset, seed.reason, seed.status
FROM (VALUES
    -- Approved, covering today or the next two weeks
    (5,  'Sick Leave',        -1,  1, 'Fever, doctor advised rest',       'Approved'),
    (8,  'Casual Leave',       0,  0, 'Personal errand',                  'Approved'),
    (2,  'Earned Leave',       2,  6, 'Family trip to Ooty',              'Approved'),
    (4,  'Work From Home',     3,  3, 'Home internet installation',       'Approved'),
    (7,  'Casual Leave',       5,  5, 'Bank work',                        'Approved'),
    (10, 'Earned Leave',       7, 11, 'Sister''s wedding',                'Approved'),
    (12, 'Compensatory Off',   9,  9, 'Worked on the weekend release',    'Approved'),
    (11, 'Paternity Leave',   12, 25, 'Birth of first child',             'Approved'),
    -- Pending, waiting in the approvals queue
    (3,  'Earned Leave',      14, 18, 'Year-end vacation',                'Pending'),
    (6,  'Casual Leave',       4,  4, 'Parent-teacher meeting',           'Pending'),
    (9,  'Sick Leave',         1,  2, 'Dental surgery',                   'Pending'),
    (13, 'Work From Home',     6,  8, 'Recovering from a minor injury',   'Pending'),
    (14, 'Earned Leave',      20, 30, 'Christmas holidays with family',   'Pending'),
    (15, 'Compensatory Off',   8,  8, 'Covered the on-call shift',        'Pending'),
    (1,  'Earned Leave',      35, 39, 'New year break',                   'Pending'),
    -- History from earlier this year
    (1,  'Sick Leave',      -150, -148, 'Viral fever',                    'Approved'),
    (1,  'Casual Leave',     -60,  -60, 'Moving house',                   'Approved'),
    (16, 'Earned Leave',     -30,  -26, 'Diwali at home',                 'Approved'),
    (17, 'Casual Leave',     -10,  -10, 'Late request, team short-staffed','Rejected')
) AS seed(employeeid, leavetype, fromoffset, tooffset, reason, status)
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- 2. Attendance for every employee on each weekday of the last three weeks.
INSERT INTO attendance (attendanceid, employeeid, attendancedate, checkin, checkout, status)
SELECT (SELECT COALESCE(MAX(attendanceid), 0) FROM attendance)
           + ROW_NUMBER() OVER (ORDER BY d.day, e.employeeid),
       e.employeeid,
       d.day,
       CASE WHEN s.status IN ('Present', 'Work From Home')
            THEN TIME '08:45' + ((e.employeeid * 7 + EXTRACT(DOY FROM d.day)::int) % 50) * INTERVAL '1 minute' END,
       CASE WHEN s.status IN ('Present', 'Work From Home')
            THEN TIME '17:45' + ((e.employeeid * 11 + EXTRACT(DOY FROM d.day)::int) % 60) * INTERVAL '1 minute' END,
       s.status
FROM employee e
CROSS JOIN (SELECT generate_series(CURRENT_DATE - 21, CURRENT_DATE - 1, INTERVAL '1 day')::date AS day) d
CROSS JOIN LATERAL (
    SELECT CASE
               WHEN EXISTS (SELECT 1 FROM leaverequest l
                            WHERE l.employeeid = e.employeeid AND l.status = 'Approved'
                              AND d.day BETWEEN l.fromdate AND l.todate) THEN 'On Leave'
               WHEN (e.employeeid + EXTRACT(DOY FROM d.day)::int) % 13 = 0 THEN 'Absent'
               WHEN (e.employeeid + EXTRACT(DOY FROM d.day)::int) % 5 = 0 THEN 'Work From Home'
               ELSE 'Present'
           END AS status
) s
WHERE EXTRACT(ISODOW FROM d.day) < 6
  AND NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- 3. Payslips for the last six full months, based on each employee's monthly salary.
INSERT INTO payroll (payrollid, employeeid, month, basicsalary, bonus, deduction, netsalary)
SELECT (SELECT COALESCE(MAX(payrollid), 0) FROM payroll) + ROW_NUMBER() OVER (ORDER BY m.month, e.employeeid),
       e.employeeid,
       to_char(m.month, 'FMMonth YYYY'),
       e.salary,
       ROUND(e.salary * ((e.employeeid + EXTRACT(MONTH FROM m.month)::int) % 4) * 0.025, 2),
       ROUND(e.salary * 0.12, 2),
       e.salary
           + ROUND(e.salary * ((e.employeeid + EXTRACT(MONTH FROM m.month)::int) % 4) * 0.025, 2)
           - ROUND(e.salary * 0.12, 2)
FROM employee e
CROSS JOIN (SELECT generate_series(date_trunc('month', CURRENT_DATE) - INTERVAL '6 months',
                                   date_trunc('month', CURRENT_DATE) - INTERVAL '1 month',
                                   INTERVAL '1 month')::date AS month) m
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- 4. A mid-year performance review for every employee.
INSERT INTO performancereview (reviewid, employeeid, reviewdate, rating, comments)
SELECT (SELECT COALESCE(MAX(reviewid), 0) FROM performancereview) + ROW_NUMBER() OVER (ORDER BY e.employeeid),
       e.employeeid,
       CURRENT_DATE - 120 + (e.employeeid % 10),
       r.rating,
       (ARRAY['Needs a clear improvement plan for the next cycle.',
              'Meets some expectations; focus on delivery timelines.',
              'Solid, dependable contributor who meets expectations.',
              'Strong ownership and quality; a go-to person on the team.',
              'Exceptional impact this half; ready for bigger responsibilities.'])[r.rating]
FROM employee e
CROSS JOIN LATERAL (SELECT CASE WHEN e.employeeid % 9 = 0 THEN 2
                                WHEN e.employeeid % 4 = 0 THEN 5
                                WHEN e.employeeid % 3 = 0 THEN 3
                                ELSE 4 END AS rating) r
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- 5. More announcements and events, including one event that has already happened.
INSERT INTO announcement (title, summary, author, postedat)
SELECT * FROM (VALUES
    ('Open enrolment for health insurance',
     'Add or update dependants on your group health policy before the end of the month.',
     'HR Team', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('New office in Coimbatore',
     'Our Coimbatore office opens next month with seats for 120 people. Ask your manager about moving.',
     'Facilities', CURRENT_TIMESTAMP - INTERVAL '8 days'),
    ('Security reminder',
     'Never share your password. IT will never ask for it over email or chat.',
     'IT Security', CURRENT_TIMESTAMP - INTERVAL '12 days')
) AS seed(title, summary, author, postedat)
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

INSERT INTO company_event (title, description, eventdate, location)
SELECT * FROM (VALUES
    ('Diwali celebration', 'Lunch, rangoli contest and games for all teams.', CURRENT_DATE - 5, 'Chennai'),
    ('Blood donation camp', 'Organised with the city blood bank. Walk in any time.', CURRENT_DATE + 6, 'Pune'),
    ('Year-end party', 'Dinner, awards and music to close the year.', CURRENT_DATE + 30, 'Mumbai')
) AS seed(title, description, eventdate, location)
WHERE NOT EXISTS (SELECT 1 FROM mock_data_seed WHERE name = 'current-activity-v1');

-- Keep the leave sequence ahead of the rows above, then record that this set is in.
SELECT setval('leaverequest_seq', (SELECT COALESCE(MAX(leaveid), 0) + 1 FROM leaverequest), false);

INSERT INTO mock_data_seed (name) VALUES ('current-activity-v1') ON CONFLICT (name) DO NOTHING;
