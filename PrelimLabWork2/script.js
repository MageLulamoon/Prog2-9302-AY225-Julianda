// ==============================
// Hardcoded login credentials
// ==============================
const VALID_USERNAME = "student";
const VALID_PASSWORD = "password123";

// ==============================
// DOM elements
// ==============================
const form = document.getElementById("login-form");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const messageDiv = document.getElementById("message");
const timestampDiv = document.getElementById("timestamp");
const attendanceListDiv = document.getElementById("attendance-list");
const downloadBtn = document.getElementById("download");
const beep = document.getElementById("beep");

// ==============================
// Attendance data
// ==============================
let attendanceRecords = [];

// ==============================
// Format date and time
// ==============================
function formatTimestamp(date) {
  const pad = n => String(n).padStart(2, "0");

  return (
    pad(date.getMonth() + 1) + "/" +
    pad(date.getDate()) + "/" +
    date.getFullYear() + " " +
    pad(date.getHours()) + ":" +
    pad(date.getMinutes()) + ":" +
    pad(date.getSeconds())
  );
}

// ==============================
// Login handler
// ==============================
form.addEventListener("submit", function (e) {
  e.preventDefault();

  const username = usernameInput.value.trim();
  const password = passwordInput.value;

  // Incorrect login
  if (username !== VALID_USERNAME || password !== VALID_PASSWORD) {
    messageDiv.textContent = "Incorrect username or password";
    messageDiv.style.color = "red";
    timestampDiv.textContent = "";
    beep.play();
    return;
  }

  // Correct login
  const now = new Date();
  const timestamp = formatTimestamp(now);

  messageDiv.textContent = "Welcome, " + username;
  timestampDiv.textContent = "Login Time: " + timestamp;

  // Save attendance
  attendanceRecords.push({
    username: username,
    timestamp: timestamp
  });

  updateAttendanceDisplay();
  passwordInput.value = "";
});

// ==============================
// Display attendance records
// ==============================
function updateAttendanceDisplay() {
  if (attendanceRecords.length === 0) {
    attendanceListDiv.textContent = "No records yet";
    return;
  }

  let output = "";
  attendanceRecords.forEach((record, index) => {
    output +=
      (index + 1) + ". Username: " + record.username +
      "\n   Timestamp: " + record.timestamp + "\n\n";
  });

  attendanceListDiv.textContent = output;
}

// ==============================
// Generate attendance file
// ==============================
downloadBtn.addEventListener("click", function () {
  if (attendanceRecords.length === 0) {
    messageDiv.textContent = "No attendance records to save";
    return;
  }

  let fileContent = "Attendance Summary\n\n";

  attendanceRecords.forEach((record, index) => {
    fileContent +=
      (index + 1) + ". Username: " + record.username +
      "\nTimestamp: " + record.timestamp + "\n\n";
  });

  const blob = new Blob([fileContent], { type: "text/plain" });
  const link = document.createElement("a");
  link.href = window.URL.createObjectURL(blob);
  link.download = "attendance_summary.txt";
  link.click();
});
