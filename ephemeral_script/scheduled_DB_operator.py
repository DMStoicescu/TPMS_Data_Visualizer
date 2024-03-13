import pymysql
import json
from time import sleep

#Global variables
connection = pymysql.connect(
	    host='sql8.freesqldatabase.com',
	    user='sql8655171',
	    password='JZVtahyQ8F',
	    db='sql8655171',
	    charset='utf8mb4',
	    cursorclass=pymysql.cursors.DictCursor
	)

table_name = "TPMSData"


#Parse from JSON to Python data structure
def jsonParser(JSON_File_Path):

	sensors_data_list = []
	# Opening JSON file
	with open(JSON_File_Path) as sensors_data_file:
		for objectEntry in sensors_data_file:
			try:
				sensors_data_dict = json.loads(objectEntry)
				sensors_data_list.append(sensors_data_dict)
			except:
				continue

	
	open(JSON_File_Path, "w").close()

	#Remove duplicate entries
	sensors_data_list = [dict(tuples) for tuples in {tuple(objectEntry.items()) for objectEntry in sensors_data_list}]

	return sensors_data_list


def insert_to_database(sensors_data):
	global connection, table_name

	connection.ping()

	if len(sensors_data) > 1:
		for sensor_entry in sensors_data:
			if "type" in sensor_entry and sensor_entry["type"] == "TPMS":

				insert_query = """INSERT INTO {tablename} (time, protocol, type, id, flags, pressure_KPa, temperature_C, status, mic, modulation, frequency_1, frequency_2, rssi, snr, noise) 
								  VALUES ('{rec_time}', '{protocol}', '{type}', '{id}', '{flags}', {pressure}, {temperature}, {status}, '{mic}', '{modulation}', {frequency_1}, {frequency_2}, {rssi}, {snr}, {noise})"""
				sensor_time = sensor_entry["time"]
				sensor_protocol = sensor_entry ["model"]
				sensor_type = sensor_entry["type"]
				sensor_id = sensor_entry["id"]
				sensor_flag = "noflag" if "flags" not in sensor_entry else sensor_entry["flags"]
				sensor_pressure = sensor_entry["pressure_kPa"]
				sensor_temperature = sensor_entry["temperature_C"]
				sensor_status = -1 if not "status" in sensor_entry  else sensor_entry["status"] #Some sensor can miss this, for consistency we make it -1 for the protocols that do not provide this information
				sensor_mic = sensor_entry["mic"]
				sensor_modulation = sensor_entry["mod"]
				sensor_frequency_1 = sensor_entry["freq1"]
				sensor_frequency_2 = sensor_entry["freq2"]
				sensor_rssi = sensor_entry["rssi"]
				sensor_snr = sensor_entry["snr"]
				sensor_noise = sensor_entry["noise"]
				insert_query = insert_query.format(tablename = table_name, rec_time = sensor_time, protocol = sensor_protocol, type = sensor_type, id = sensor_id, flags = sensor_flag, pressure = sensor_pressure,
												   temperature = sensor_temperature, status = sensor_status, mic = sensor_mic, modulation = sensor_modulation, frequency_1 = sensor_frequency_1, frequency_2 = sensor_frequency_2,
												   rssi = sensor_rssi, snr = sensor_snr, noise = sensor_noise)
				print(insert_query)
				try:
					connection.ping()
					with connection.cursor() as cursor:         
						cursor.execute(insert_query)
						connection.commit()
				except:
					print("Did not complete insertion to database")

				# order_by_time_query = "SELECT * FROM `TPMSData` ORDER BY `TPMSData`.`time` ASC "
				# cursor.execute(order_by_time_query)
				# connection.commit()

		print("Insertion of data into the database completed")


	else:
		
		print("Could not perform insert operation, sensor data is empty in this cycle")
	


def main():
	global connection, table_name

	print("Starting cycle...")

	sensors_data = jsonParser('sensors_data1.json')

	insert_to_database(sensors_data)

	# cursor = connection.cursor()
	# delete_query = "DELETE FROM " + table_name
	# cursor.execute(delete_query)
	# connection.commit()

	connection.close()
	print("Cycle ended, entering passive mode until cycle restart...")

if __name__=="__main__": 
	while True:
		main()
		sleep(60)
		print("Restarting cycle...")
