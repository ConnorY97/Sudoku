import defusedxml.ElementTree as ET
import json
import argparse
import os
from datetime import datetime, timezone
import traceback


def to_unix_timestamp(dt_str):
    """
    Converts an ISO 8601 datetime string to a Unix timestamp in seconds.
    Returns 0 if conversion fails.
    """
    try:
        dt = datetime.strptime(dt_str, "%Y-%m-%d %H:%M:%S%z")
        return int(dt.timestamp())
    except Exception as e:
        print(f"Error parsing datetime '{dt_str}': {e}")
        return 0


def convert_xml_to_json(xml_file_path, output_file_path):
    try:
        # Parse the XML file
        tree = ET.parse(xml_file_path)
        root = tree.getroot()
        print('Retrieved tree')

        # Check if the root is the main test suite
        if root.tag == "testsuite":
            main_suite = root
        else:
            main_suite = root.find("./testsuite")

        if main_suite is None:
            print("Warning: No 'testsuite' element found. Available elements:")
            for elem in root:
                print(f"Element: {elem.tag}, Attributes: {elem.attrib}")
            raise ValueError("The XML structure is missing the expected 'testsuite' element.")

        # Extract times and duration
        start_time = main_suite.attrib.get("timestamp", "0001-01-01 00:00:00+0000")
        duration = float(main_suite.attrib.get("time", 0))
        start_timestamp = to_unix_timestamp(start_time)
        stop_timestamp = start_timestamp + int(duration)

        print("Retrieved times")

        # General information
        total_tests = int(main_suite.attrib.get("tests", 0))
        skipped_tests = int(main_suite.attrib.get("skipped", 0))
        failed_tests = int(main_suite.attrib.get("failures", 0))
        passed_tests = total_tests - skipped_tests - failed_tests

        print("Retrieved test amounts")

        # Prepare test details
        total_duration = 0
        tests = []
        for testcase in main_suite.findall(".//testcase"):
            name = testcase.attrib.get("name", "Unnamed Test")
            status = "failed" if testcase.find(".//failure") is not None else "passed"
            suite = testcase.attrib.get("classname", "Unknown Suite")
            duration = float(testcase.attrib.get("time", 0)) * 1000  # Convert to ms
            total_duration += duration
            test_start = start_timestamp
            test_stop = test_start + int(duration)

            # Handle failure details (message and stack trace)
            message = None
            trace = None
            # Handle failure details
            failure_element = testcase.find(".//failure")
            if failure_element is not None:
                # Extract and clean up the 'message' attribute
                raw_message = failure_element.attrib.get("message", "No message provided")
                if raw_message.startswith("junit.framework.AssertionFailedError:"):
                    message = raw_message.replace("junit.framework.AssertionFailedError:", "").strip()
                else:
                    message = raw_message.strip()

                # Extract and optionally trim the trace
                trace = failure_element.text.strip() if failure_element.text else "No trace available"
                trace_lines = trace.splitlines()
                if len(trace_lines) > 5:  # Limit to the top 5 lines if trace is too long
                    trace = "\n".join(trace_lines[:5]) + "\n... (truncated)"
            else:
                message = "Test passed"
                trace = None



            # Create the test entry
            test_entry = {
                "name": name,
                "status": status,
                "duration": int(duration),
                "start": test_start,
                "stop": test_stop,
                "suite": suite,
                "rawStatus": status,
                "tags": ["ExampleTag"],  # Placeholder tags
                "type": "e2e",  # Example type
                "filePath": f"/tests/{suite.replace('.', '/')}/{name}.test.js",  # Generate a file path
                "retries": 0,
                "flaky": False,
                "browser": "Unknown",  # Placeholder browser
                "extra": {},
                "message": message,
                "trace": trace,
                "screenshot": None  # Screenshot placeholder
            }
            tests.append(test_entry)

        stop_timestamp = start_timestamp + int(total_duration)

        # Summary Section
        summary = {
            "tests": total_tests,
            "passed": passed_tests,
            "failed": failed_tests,
            "pending": 0,  # No pending info in XML
            "skipped": skipped_tests,
            "other": 0,  # No "other" info in XML
            "suites": 1,  # Root is the main suite
            "start": start_timestamp,
            "stop": stop_timestamp,
            "duration": int(total_duration)  # Convert to ms
        }

        # Final JSON structure
        final_output = {
            "results": {
                "tool": {
                    "name": "Sudoku",  # Example tool name
                    "version": "1.0"  # Example version
                },
                "summary": summary,
                "tests": tests
            }
        }

        # Write JSON file
        os.makedirs(os.path.dirname(output_file_path), exist_ok=True)
        with open(output_file_path, "w") as json_file:
            json.dump(final_output, json_file, indent=4)

        print(f"Conversion successful! JSON saved to {output_file_path}")

    except Exception as e:
        print(f"Error during conversion: {e}")
        traceback.print_exc()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Convert XML test results to JSON.")
    parser.add_argument("xml_file", help="The input XML file containing test results.")
    args = parser.parse_args()
    try:
        # Generate JSON data

        json_output_file = "/home/connor/Documents/SudokuResults/results.json"
        print(json_output_file)

        # Ensure the output directories exist
        os.makedirs(os.path.dirname(json_output_file), exist_ok=True)
        print("Created file")

        convert_xml_to_json(args.xml_file, json_output_file)

    except FileNotFoundError as e:
        print(f"File not found with error: {e}")
        exit(1)
    except Exception as e:
        traceback.print_exc()
        print(f"Failed to convert XML to JSON: {e}")
