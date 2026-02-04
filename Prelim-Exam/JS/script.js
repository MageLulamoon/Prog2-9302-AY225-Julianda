// Programmer: Jurrien Julianda / Student ID: 25-0909-131
// script.js - parse hardcoded CSV and implement CRUD for the web version

// Multi-line CSV string (header + sample rows) pulled from MOCK_DATA.csv for reference.
// The parsing function is robust: it will use StudentID, first_name, last_name columns when present
// and compute a rounded average of numeric columns to produce the Grade column.
const csvData = `
StudentID,first_name,last_name,LAB WORK 1,LAB WORK 2,LAB WORK 3,PRELIM EXAM,ATTENDANCE GRADE
073900438,Osbourne,Wakenshaw,69,5,52,12,78
114924014,Albie,Gierardi,58,92,16,57,97
111901632,Eleen,Pentony,43,81,34,36,16
084000084,Arie,Okenden,31,5,14,39,99
272471551,Alica,Muckley,49,66,97,3,95
104900721,Jo,Burleton,98,94,33,13,29
111924392,Cam,Akram,44,84,17,16,24
292970744,Celine,Brosoli,3,15,71,83,45
107004352,Alan,Belfit,31,51,36,70,48
071108313,Jeanette,Gilvear,4,78,15,69,69
042204932,Ethelin,MacCathay,48,36,23,1,11
111914218,Kakalina,Finnick,69,5,65,10,8
074906059,Mayer,Lorenzetti,36,30,100,41,92
091000080,Selia,Rosenstengel,15,42,85,68,28
055002480,Dalia,Tadd,84,86,13,91,22
063101111,Darryl,Doogood,36,3,78,13,100
071908827,Brier,Wace,69,92,23,75,40
322285668,Bucky,Udall,97,63,19,46,28
103006406,Haslett,Beaford,41,32,85,60,61
104913048,Shelley,Spring,84,73,63,59,3
051403517,Marius,Southway,28,75,29,88,92
021301869,Katharina,Storch,6,61,6,49,56
063115178,Hester,Menendez,70,46,73,40,56
084202442,Shaylynn,Scorthorne,50,80,81,96,83
275079882,Madonna,Willatt,23,12,17,83,5
071001041,Bancroft,Padfield,50,100,58,13,14
`;

// Global data array
let records = [];

// parse CSV into records array of { id, name, grade }
function parseCSV(raw) {
  const lines = raw.trim().split(/\r?\n/).map(l => l.trim()).filter(Boolean);
  if (lines.length === 0) return [];
  const header = lines[0].split(',').map(h => h.trim().toLowerCase());
  const hasStudentId = header.includes('studentid');
  const hasFirst = header.includes('first_name') || header.includes('first name') || header.includes('first');
  const hasLast = header.includes('last_name') || header.includes('last name') || header.includes('last');

  // find indices
  const idxStudent = header.findIndex(h => h === 'studentid') >= 0 ? header.findIndex(h => h === 'studentid') : 0;
  const idxFirst = header.findIndex(h => h === 'first_name' || h === 'first name' || h === 'first');
  const idxLast = header.findIndex(h => h === 'last_name' || h === 'last name' || h === 'last');

  const out = [];
  for (let i = 1; i < lines.length; i++) {
    const parts = lines[i].split(',').map(p => p.trim());
    if (parts.length === 0) continue;
    const id = parts[idxStudent] || parts[0] || "";
    let name = "";
    let grade = "";

    if (hasFirst && hasLast && idxFirst >= 0 && idxLast >= 0 && parts[idxFirst] && parts[idxLast]) {
      name = parts[idxFirst] + " " + parts[idxLast];
      // numeric columns likely start after last name
      const start = Math.max(idxLast + 1, 2);
      grade = computeAverage(parts.slice(start));
    } else if (parts.length === 3 && !isNumeric(parts[1])) {
      // assume ID, Name, Grade or ID, First, Last (but last is not numeric)
      if (isNumeric(parts[2])) {
        name = parts[1];
        grade = parts[2];
      } else {
        name = parts[1] + " " + parts[2];
        grade = "";
      }
    } else {
      // fallback: name = parts[1], average numeric from parts[2...]
      name = parts[1] || "";
      grade = computeAverage(parts.slice(2));
    }
    out.push({ id: id, name: name, grade: grade });
  }
  return out;
}

function computeAverage(arr) {
  let sum = 0, cnt = 0;
  for (let s of arr) {
    s = s.replace(/[^0-9\.\-]/g, '').trim();
    if (s === '') continue;
    const n = parseFloat(s);
    if (!isNaN(n)) { sum += n; cnt++; }
  }
  if (cnt === 0) return "";
  return String(Math.round(sum / cnt));
}

function isNumeric(s) {
  if (!s) return false;
  return !isNaN(parseFloat(s));
}

// render table
function render() {
  const tbody = document.getElementById('tbody');
  tbody.innerHTML = '';
  records.forEach((r, idx) => {
    const row = `
      <tr>
        <td>${escapeHtml(r.id)}</td>
        <td>${escapeHtml(r.name)}</td>
        <td>${escapeHtml(r.grade)}</td>
        <td><button class="action-btn" data-index="${idx}">Delete</button></td>
      </tr>
    `;
    tbody.insertAdjacentHTML('beforeend', row);
  });

  // attach delete listeners
  document.querySelectorAll('.action-btn').forEach(btn => {
    btn.addEventListener('click', function () {
      const i = parseInt(this.getAttribute('data-index'));
      if (!Number.isNaN(i)) {
        records.splice(i, 1);
        render();
      }
    });
  });
}

function escapeHtml(s) {
  if (s === null || s === undefined) return '';
  return String(s).replace(/[&<>"']/g, function (m) {
    return ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[m];
  });
}

// add new record from inputs
function addRecord() {
  const id = document.getElementById('inpId').value.trim();
  const name = document.getElementById('inpName').value.trim();
  const grade = document.getElementById('inpGrade').value.trim();
  if (!id && !name) {
    alert('Enter at least an ID or Name');
    return;
  }
  records.push({ id: id, name: name, grade: grade });
  document.getElementById('inpId').value = '';
  document.getElementById('inpName').value = '';
  document.getElementById('inpGrade').value = '';
  render();
}

// init
document.addEventListener('DOMContentLoaded', function () {
  // parse CSV into records
  records = parseCSV(csvData);

  // initial render
  render();

  // wire add button
  document.getElementById('btnAdd').addEventListener('click', addRecord);

  // enter key behavior for grade input
  document.getElementById('inpGrade').addEventListener('keydown', function (ev) {
    if (ev.key === 'Enter') addRecord();
  });
});
