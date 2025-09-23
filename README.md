# Science Fair Table Assignment Tool

A Java-based standalone application for automatically assigning science fair projects to table slots based on requirements and compatibility scoring.

## Features

- **Smart Assignment Algorithm**: Assigns projects to tables based on:
  - Space requirements
  - Electricity and water needs
  - Category matching
  - Grade-level appropriateness
  - Special requirements

- **Dual Interface**: 
  - GUI mode for easy file selection
  - Command-line mode for automated processing

- **Cross-Platform**: Runs on macOS 26+, Windows, and Linux with Java 11+

## Requirements

- Java 11 or higher
- CSV files with project and table data

## Installation

1. Ensure Java 11+ is installed on your system
2. Download the `science-fair-table-assignment.jar` file
3. No additional installation required

## Usage

### GUI Mode (Recommended)

Run the application without arguments to launch the graphical interface:

```bash
java -jar science-fair-table-assignment.jar
```

1. Click "Browse" to select your projects CSV file
2. Click "Browse" to select your tables CSV file  
3. Choose output location (defaults to Desktop)
4. Click "Assign Projects to Tables"
5. View results and summary in the application

### Command Line Mode

For automated processing or scripting:

```bash
java -jar science-fair-table-assignment.jar projects.csv tables.csv output.csv
```

Example:
```bash
java -jar science-fair-table-assignment.jar sample_projects.csv sample_tables.csv assignments.csv
```

## CSV File Formats

### Projects CSV Format

Required headers (in any order):
```
projectId,projectName,studentName,category,grade,requiresElectricity,requiresWater,specialRequirements,estimatedSpace
```

**Field Descriptions:**
- `projectId`: Unique identifier for the project
- `projectName`: Name of the science fair project
- `studentName`: Name of the student
- `category`: Project category (e.g., Biology, Chemistry, Physics, Engineering, Earth Science)
- `grade`: Student grade level (K, 1, 2, 3, ..., 12)
- `requiresElectricity`: true/false or yes/no or 1/0
- `requiresWater`: true/false or yes/no or 1/0
- `specialRequirements`: Any special needs (text, can be empty)
- `estimatedSpace`: Required space in square feet (number)

### Tables CSV Format

Required headers (in any order):
```
tableId,location,capacity,hasElectricity,hasWater,category,gradeRange,isAccessible,notes
```

**Field Descriptions:**
- `tableId`: Unique identifier for the table
- `location`: Physical location description
- `capacity`: Available space in square feet (number)
- `hasElectricity`: true/false or yes/no or 1/0
- `hasWater`: true/false or yes/no or 1/0
- `category`: Preferred project category (can be empty for general use)
- `gradeRange`: Preferred grade range (e.g., "K-2", "3-5", "6-8", "9-12", can be empty)
- `isAccessible`: Wheelchair accessible true/false or yes/no or 1/0
- `notes`: Additional information (text, can be empty)

### Output CSV Format

The tool generates an output CSV with these columns:
```
tableId,projectId,studentName,projectName,category,assignmentReason,compatibilityScore
```

- Tables with assigned projects will have all fields filled
- Unassigned tables will have empty project fields
- `assignmentReason` explains why the assignment was made
- `compatibilityScore` indicates the quality of the match (higher = better)

## Assignment Algorithm

The algorithm prioritizes assignments based on:

1. **Project Priority:**
   - Projects with special requirements (electricity/water) first
   - Larger space requirements next
   - Younger grades first (better table access)

2. **Table Compatibility:**
   - Must meet space requirements
   - Must provide required utilities (electricity/water)
   - Bonus points for category matching
   - Bonus points for appropriate grade range
   - Efficiency scoring for space utilization

3. **Assignment Process:**
   - Each project assigned to best available table
   - Tables marked as unavailable after assignment
   - Remaining tables marked as unassigned

## Sample Files

The project includes sample CSV files to demonstrate the format:
- `sample_projects.csv`: Example project data with 15 projects
- `sample_tables.csv`: Example table data with 20 tables

## Building from Source

If you want to build the application yourself:

```bash
# Clone or download the source code
cd ScienceFair

# Build with Maven
mvn clean package

# The executable JAR will be created in target/science-fair-table-assignment.jar
```

## Troubleshooting

### Java Not Found
- Ensure Java 11+ is installed
- Check that `java` command is in your system PATH
- On macOS, you may need to install Java from Oracle or use OpenJDK

### File Not Found Errors
- Verify CSV file paths are correct
- Ensure CSV files have the required headers
- Check file permissions

### Assignment Issues
- Verify CSV data is properly formatted
- Check that table capacities can accommodate project space needs
- Ensure tables with required utilities exist for projects that need them

### CSV Format Issues
- Use commas as separators
- Include header row with exact column names
- Use quotes around text containing commas
- Boolean fields accept: true/false, yes/no, 1/0

## License

This project is provided as-is for educational and organizational use.

## Version

1.0.0 - Initial release