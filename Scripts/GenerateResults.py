import defusedxml.ElementTree as ET
import json
import argparse
import os
from datetime import datetime, timezone
import traceback

# def to_unix_timestamp(dt_str):
#     """
#     Converts an ISO 8601 datetime string to a Unix timestamp in seconds.
#     Handles cases with and without time zone information.
#     Returns 0 if conversion fails.
#     """
#     try:
#         # Attempt parsing with time zone
#         if "+" in dt_str or "-" in dt_str[-6:]:  # Likely contains a time zone
#             dt = datetime.strptime(dt_str, "%Y-%m-%d %H:%M:%S%z")
#         else:
#             # Assume UTC if no time zone provided
#             dt = datetime.strptime(dt_str, "%Y-%m-%d %H:%M:%S")
#             dt = dt.replace(tzinfo=timezone.utc)
#         return int(dt.timestamp())
#     except Exception as e:
#         print(f"Error parsing datetime '{dt_str}': {e}")
#         return 0

def convert_unit_test_to_json(xml_file_path, output_file_path):
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
        start_timestamp = 0
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

def convert_instrument_test_to_json(xml_file_path, output_file_path):
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
        start_timestamp = 0 #to_unix_timestamp(start_time)
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

            # Handle failure details
            failure_element = testcase.find(".//failure")
            if failure_element is not None:
                # Extract and clean up the 'message' and 'trace'
                message = failure_element.attrib.get("message", None)
                if not message:  # If no message attribute, extract from text content
                    raw_message = failure_element.text.strip() if failure_element.text else "No message provided"
                    if raw_message.startswith("junit.framework.AssertionFailedError:"):
                        message = raw_message.replace("junit.framework.AssertionFailedError:", "").strip()
                        message = message.split("\n", 1)[0]
                    else:
                        message = raw_message.strip()

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
    parser.add_argument("unit_test_dir", help="Path to the unit test xml")
    parser.add_argument("instrument_test_dir", help="Path to the instrument test xml")
    args = parser.parse_args()
    try:
        # Generate JSON data
        output_dir = r"/home/connor/Documents/SudokuResults/"

        # FOR LOCAL TESTING
        local_output_dir = "C:\\Users\\c_you\\Desktop\\tmp\\"

        # MAKE SURE TO SET TO FALSE ONCE LOCAL TESTING DONE
        local_testing = False
        print(f"LOCAL TESTING: {local_testing}")
        # !!

        if local_testing:
            if (os.path.exists(args.unit_test_dir) and os.path.isdir(args.unit_test_dir)):
                for filename in os.listdir(args.unit_test_dir):
                    if filename.endswith(".xml"):
                        print(f"Converting {filename}")
                        input_file = os.path.join(args.unit_test_dir, filename)
                        output_file = os.path.join(local_output_dir, filename.replace(".xml", ".json"))
                        convert_unit_test_to_json(input_file, output_file)
                    else:
                        print(f"{os.path.join(args.unit_test_dir, filename)} is not an xml file")
            else:
                print(f"Could not find unit test output dir at {args.unit_test_dir}")

            if (os.path.exists(args.instrument_test_dir) and os.path.isdir(args.instrument_test_dir)):
                for filename in os.listdir(args.instrument_test_dir):
                    if filename.endswith(".xml"):
                        print(f"Converting {filename}")
                        input_file = os.path.join(args.instrument_test_dir, filename)
                        output_file = os.path.join(local_output_dir, filename.replace(".xml", ".json"))
                        print(f"{output_file}")
                        convert_instrument_test_to_json(input_file, output_file)
                    else:
                        print(f"{os.path.join(args.instrument_test_dir, filename)} is not an xml file")
            else:
                print(f"Could not find unit test output file at {args.instrument_test_dir}")
        else:
            if (os.path.exists(args.unit_test_dir) and os.path.isdir(args.unit_test_dir)):
                for filename in os.listdir(args.unit_test_dir):
                    if filename.endswith(".xml"):
                        print(f"Converting {filename}")
                        input_file = os.path.join(args.unit_test_dir, filename)
                        output_file = os.path.join(output_dir, filename.replace(".xml", ".json"))
                        convert_unit_test_to_json(input_file, output_file)
                    else:
                        print(f"{os.path.join(args.unit_test_dir, filename)} is not an xml file")
            else:
                print(f"Could not find unit test output dir at {args.unit_test_dir}")

            if (os.path.exists(args.instrument_test_dir) and os.path.isdir(args.instrument_test_dir)):
                for filename in os.listdir(args.instrument_test_dir):
                    if filename.endswith(".xml"):
                        print(f"Converting {filename}")
                        input_file = os.path.join(args.instrument_test_dir, filename)
                        output_file = os.path.join(output_dir, filename.replace(".xml", ".json"))
                        print(f"{output_file}")
                        convert_instrument_test_to_json(input_file, output_file)
                    else:
                        print(f"{os.path.join(args.instrument_test_dir, filename)} is not an xml file")
            else:
                print(f"Could not find unit test output file at {args.instrument_test_dir}")

    except FileNotFoundError as e:
        print(f"File not found with error: {e}")
        exit(1)
    except Exception as e:
        traceback.print_exc()
        print(f"Failed to convert XML to JSON: {e}")
