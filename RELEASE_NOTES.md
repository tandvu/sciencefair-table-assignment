# Science Fair Table Assignment - Release Package

## Files Included

### Application
- `science-fair-table-assignment.jar` - Standalone executable JAR file

### Documentation
- `README.md` - Complete user guide and documentation
- `sample_projects.csv` - Example project data (15 projects)
- `sample_tables.csv` - Example table data (20 tables)

### Source Code (Development)
- Complete Maven project source code
- Built with Java 11 for compatibility with macOS 26+

## Quick Start

1. **Install Java 11+** on your macOS system
   ```bash
   # Check if Java is installed
   java -version
   
   # If not installed, download from:
   # https://adoptium.net/temurin/releases/
   ```

2. **Run the Application**
   
   **GUI Mode (Recommended):**
   ```bash
   java -jar science-fair-table-assignment.jar
   ```
   
   **Command Line Mode:**
   ```bash
   java -jar science-fair-table-assignment.jar projects.csv tables.csv output.csv
   ```

3. **Test with Sample Data**
   ```bash
   java -jar science-fair-table-assignment.jar sample_projects.csv sample_tables.csv my_assignments.csv
   ```

## Assignment Results

The application will:
- ✅ Prioritize projects with special requirements (electricity/water)
- ✅ Match projects to compatible tables based on space and utilities
- ✅ Prefer category and grade-level matching when possible
- ✅ Generate assignments for all table slots (assigned or unassigned)
- ✅ Provide detailed assignment reasons and compatibility scores
- ✅ Display summary statistics of the assignment process

## System Requirements

- **Operating System**: macOS 26+ (also compatible with Windows and Linux)
- **Java Version**: Java 11 or higher
- **Memory**: Minimal requirements (works with small to large datasets)
- **Display**: GUI mode requires graphical display; command-line mode works headless

## File Format Validation

The application validates:
- Required CSV headers are present
- Data types are correct (numbers for space/capacity, booleans for yes/no fields)
- File accessibility and permissions
- Table capacity vs. project space requirements

## Support

For issues or questions:
1. Check the README.md for detailed documentation
2. Verify CSV file formats match the required headers
3. Ensure Java 11+ is properly installed
4. Test with the provided sample files first

---

**Version**: 1.0.0  
**Build Date**: September 2025  
**Compatibility**: Cross-platform (Java 11+)